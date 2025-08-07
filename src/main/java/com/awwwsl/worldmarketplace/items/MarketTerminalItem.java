package com.awwwsl.worldmarketplace.items;

import com.awwwsl.worldmarketplace.MarketSavedData;
import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import com.awwwsl.worldmarketplace.blocks.MarketBlockEntity;
import com.awwwsl.worldmarketplace.blocks.MarketTerminalBlockEntity;
import com.awwwsl.worldmarketplace.display.MarketTerminalMenuProvider;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MarketTerminalItem extends BlockItem {
    public MarketTerminalItem() {
        super(WorldmarketplaceMod.MARKET_TERMINAL_BLOCK.get(), new Item.Properties()
                .food(new FoodProperties.Builder()
                    .alwaysEat()
                    .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 1), 100f)
                    .nutrition(1)
                    .saturationMod(0.5F)
                    .build())
        );
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext useOnContext) {
        if(useOnContext.getPlayer() == null) {
            return InteractionResult.PASS;
        }
        if(useOnContext.getPlayer().isShiftKeyDown()) {
            // check if the clicked block is a MarketBlockEntity and save it to item if it is
            var pos = useOnContext.getClickedPos();
            var level = useOnContext.getLevel();
            var player = useOnContext.getPlayer();
            var blockEntity = level.getBlockEntity(pos);
            if(level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer && !serverLevel.isClientSide) {
                if(blockEntity instanceof MarketBlockEntity marketBlockEntity) {
                    if(marketBlockEntity.getMarket() == null) {
                        player.displayClientMessage(Component.translatable("worldmarketplace.market_terminal.no_market"), true);
                        return InteractionResult.FAIL;
                    }
                    var itemStack = useOnContext.getItemInHand();
                    if(!itemStack.isEmpty() && itemStack.getItem() instanceof MarketTerminalItem self) {
                        saveMarketPosition(itemStack, marketBlockEntity.getMarketChunk());
                        serverPlayer.sendSystemMessage(Component.literal("已保存市场位置: " + pos)); // TODO: use translatable
                        return InteractionResult.sidedSuccess(level.isClientSide);
                    }
                    else {
                        return InteractionResult.PASS; // what
                    }
                }
            } else {
                if(blockEntity instanceof MarketBlockEntity marketBlockEntity) {
                    return InteractionResult.SUCCESS;
                }
            }

            // block place logic
            if(useOnContext.getClickedFace() == Direction.DOWN || useOnContext.getClickedFace() == Direction.UP) {
                return InteractionResult.FAIL;
            }
            var itemStack = useOnContext.getItemInHand();
            var marketChunk = getMarketPosition(itemStack);
            var result = super.useOn(useOnContext);
            blockEntity = level.getBlockEntity(pos.relative(useOnContext.getClickedFace()));
            if(marketChunk != null && result == InteractionResult.CONSUME) {
                if(level instanceof ServerLevel serverLevel && !serverLevel.isClientSide) {
                    if(blockEntity instanceof MarketTerminalBlockEntity marketTerminalBlockEntity) {
                        marketTerminalBlockEntity.setMarketChunk(marketChunk);
                        marketTerminalBlockEntity.setChanged();
                        serverLevel.sendBlockUpdated(
                            marketTerminalBlockEntity.getBlockPos(),
                            marketTerminalBlockEntity.getBlockState(),
                            marketTerminalBlockEntity.getBlockState(),
                            3
                        );
                    } else {
                        // so how can one place a MarketTerminalBlock without a MarketTerminalBlockEntity?
                        WorldmarketplaceMod.LOGGER.warn("MarketTerminalBlockEntity not found at position: {}", useOnContext.getClickedPos());
                    }
                }
            }
            return result;
        }

        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResult place(@NotNull BlockPlaceContext blockPlaceContext) {
        var stickedPos = blockPlaceContext.getClickedPos().relative(blockPlaceContext.getClickedFace().getOpposite());
        var level = blockPlaceContext.getLevel();
        var stickedState = level.getBlockState(stickedPos);
        if (!stickedState.isFaceSturdy(level, stickedPos, blockPlaceContext.getClickedFace())) {
            return InteractionResult.FAIL;
        }
        return super.place(blockPlaceContext);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        var itemStack = player.getItemInHand(hand);
        var marketChunk = getMarketPosition(itemStack);
        if (marketChunk != null) {
            if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer && !level.isClientSide) {
                var market = MarketSavedData.get(serverLevel).getMarket(serverLevel, marketChunk);
                if(market != null) {
                    var provider = new MarketTerminalMenuProvider(market);
                    NetworkHooks.openScreen(serverPlayer, provider, provider::clientMenu);
                } else {
                    player.sendSystemMessage(Component.literal("市场位置已失效")); // TODO: use translatable
                    return InteractionResultHolder.fail(player.getItemInHand(hand));
                }
                return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
            } else {
                return InteractionResultHolder.fail(player.getItemInHand(hand));
            }
        } else {
            player.sendSystemMessage(Component.literal("请先保存市场位置。使用Shift+右键点击市场终端来保存位置。")); // TODO: use translatable
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
    }

    private static void saveMarketPosition(ItemStack selfStack, ChunkPos chunkPos) {
        if (selfStack.isEmpty() && selfStack.getItem() instanceof MarketTerminalItem) {
            throw new IllegalArgumentException("ItemStack is not a MarketTerminalItem");
        }

        selfStack.getOrCreateTag().putLong("marketPos", chunkPos.toLong());
    }

    public static @Nullable ChunkPos getMarketPosition(ItemStack selfStack) {
        if (selfStack.isEmpty() || !(selfStack.getItem() instanceof MarketTerminalItem)) {
            throw new IllegalArgumentException("ItemStack is not a MarketTerminalItem");
        }

        if (selfStack.getOrCreateTag().contains("marketPos")) {
            long posLong = selfStack.getOrCreateTag().getLong("marketPos");
            return new ChunkPos(posLong);
        } else {
            return null;
        }
    }
}
