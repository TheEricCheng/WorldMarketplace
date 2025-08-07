package com.awwwsl.worldmarketplace.items;

import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import com.awwwsl.worldmarketplace.blocks.MarketBlockEntity;
import com.awwwsl.worldmarketplace.blocks.MarketTerminalBlock;
import com.awwwsl.worldmarketplace.display.MarketTerminalMenu;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

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
            if(level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer && !serverLevel.isClientSide) {
                var blockEntity = serverLevel.getBlockEntity(pos);
                if(blockEntity instanceof MarketBlockEntity marketBlockEntity) {
                    if(marketBlockEntity.getMarket() == null) {
                        player.displayClientMessage(Component.translatable("worldmarketplace.market_terminal.no_market"), true);
                        return InteractionResult.FAIL;
                    }
                    var itemStack = useOnContext.getItemInHand();
                    if(!itemStack.isEmpty() && itemStack.getItem() instanceof MarketTerminalItem self) {
                        saveMarketPosition(itemStack, new ChunkPos(pos));
                        serverPlayer.sendSystemMessage(Component.literal("已保存市场位置: " + pos)); // TODO: use translatable
                        return InteractionResult.sidedSuccess(false);
                    }
                    else {
                        return InteractionResult.PASS; // what
                    }
                }
            } else {
                return InteractionResult.SUCCESS;
            }

            // block place logic
            if(useOnContext.getClickedFace() == Direction.DOWN || useOnContext.getClickedFace() == Direction.UP) {
                return InteractionResult.FAIL;
            }

            return super.useOn(useOnContext);
        }

        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        if (!level.isClientSide) {
            player.openMenu(new SimpleMenuProvider((id, inventory, p) ->
                new MarketTerminalMenu(id, inventory), Component.literal("我的菜单")));
        }

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    private static void saveMarketPosition(ItemStack selfStack, ChunkPos chunkPos) {
        if (selfStack.isEmpty() && selfStack.getItem() instanceof MarketTerminalItem) {
            throw new IllegalArgumentException("ItemStack is not a MarketTerminalItem");
        }

        selfStack.getOrCreateTag().putLong("marketPos", chunkPos.toLong());
    }
}
