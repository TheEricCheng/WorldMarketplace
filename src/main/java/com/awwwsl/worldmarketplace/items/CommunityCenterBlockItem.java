package com.awwwsl.worldmarketplace.items;

import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import com.awwwsl.worldmarketplace.blocks.CommuniyCenterBlockEntity;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

public class CommunityCenterBlockItem extends BlockItem {
    public CommunityCenterBlockItem() {
        super(WorldmarketplaceMod.COMMUNITY_CENTER_BLOCK.get(),
              new Properties());
    }

    @Override
    public @NotNull InteractionResult place(BlockPlaceContext ctx) {
        if (!this.getBlock().isEnabled(ctx.getLevel().enabledFeatures())) return InteractionResult.FAIL;
        if (!ctx.canPlace()) return InteractionResult.FAIL;

        BlockPlaceContext updatedCtx = this.updatePlacementContext(ctx);
        if (updatedCtx == null) return InteractionResult.FAIL;

        BlockState mainState = this.getPlacementState(updatedCtx);
        if (mainState == null) return InteractionResult.FAIL;

        BlockPos origin = updatedCtx.getClickedPos();
        Direction facing = ctx.getHorizontalDirection(); // 面向放置者

        if (!canPlaceStructure(ctx.getLevel(), origin, facing)) {
            if (ctx.getPlayer() != null && ctx.getLevel().isClientSide) {
                ctx.getPlayer().displayClientMessage(Component.literal("空间不足"), true);
            }
            return InteractionResult.FAIL;
        }
        // 先放置主方块
        if (!this.placeBlock(updatedCtx, mainState)) return InteractionResult.FAIL;

        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();
        BlockState actualState = level.getBlockState(origin);

        // 后续初始化逻辑
        actualState = this.updateBlockStateFromTag(origin, level, stack, actualState);
        this.updateCustomBlockEntityTag(origin, level, player, stack, actualState);
        actualState.getBlock().setPlacedBy(level, origin, actualState, player, stack);

        // ✅ 放置结构其余部分
        placeStructure(level, origin, facing);

        // 成就 & 声音 & 减少物品
        if (player instanceof ServerPlayer sp) {
            CriteriaTriggers.PLACED_BLOCK.trigger(sp, origin, stack);
        }

        SoundType soundtype = actualState.getSoundType(level, origin, player);
        level.playSound(player, origin, this.getPlaceSound(actualState, level, origin, player),
            SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
        level.gameEvent(GameEvent.BLOCK_PLACE, origin, GameEvent.Context.of(player, actualState));

        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private BlockState updateBlockStateFromTag(BlockPos p_40603_, Level p_40604_, ItemStack p_40605_, BlockState p_40606_) {
        BlockState blockstate = p_40606_;
        CompoundTag compoundtag = p_40605_.getTag();
        if (compoundtag != null) {
            CompoundTag compoundtag1 = compoundtag.getCompound("BlockStateTag");
            StateDefinition<Block, BlockState> statedefinition = p_40606_.getBlock().getStateDefinition();

            for(String s : compoundtag1.getAllKeys()) {
                Property<?> property = statedefinition.getProperty(s);
                if (property != null) {
                    String s1 = compoundtag1.get(s).getAsString();
                    blockstate = updateState(blockstate, property, s1);
                }
            }
        }

        if (blockstate != p_40606_) {
            p_40604_.setBlock(p_40603_, blockstate, 2);
        }

        return blockstate;
    }

    private static <T extends Comparable<T>> BlockState updateState(BlockState p_40594_, Property<T> p_40595_, String p_40596_) {
        return p_40595_.getValue(p_40596_).map((p_40592_) -> {
            return p_40594_.setValue(p_40595_, p_40592_);
        }).orElse(p_40594_);
    }

    @Override
    protected boolean canPlace(@NotNull BlockPlaceContext blockPlaceContext, @NotNull BlockState blockState) {
        return canPlaceStructure(
            blockPlaceContext.getLevel(),
            blockPlaceContext.getClickedPos(),
            blockPlaceContext.getHorizontalDirection()
        );
    }

    private boolean canPlaceStructure(Level level, BlockPos origin, Direction facing) {
        // 计算朝向 -> XZ 平面旋转偏移
        Direction right = facing.getClockWise(); // 向右偏移方向

        for(int dx = -1; dx <= 1; dx++) {
            for(int dy = 0; dy <= 2; dy++) {
                BlockPos checking = origin.offset(right.getStepX() * dx, dy, right.getStepZ() * dx);
                BlockState checkingState = level.getBlockState(checking);
                if(!checkingState.isAir() && !checkingState.canBeReplaced()){
                    return false;
                }
            }
        }

        return true;
    }

    private void placeStructure(Level level, BlockPos origin, Direction facing) {
        Direction right = facing.getClockWise();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy <= 2; dy++) {
                BlockPos checking = origin.offset(right.getStepX() * dx, dy, right.getStepZ() * dx);
                BlockState target = WorldmarketplaceMod.COMMUNITY_CENTER_BLOCK.get().defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, facing.getOpposite());
                if (!checking.equals(origin)) {
                    level.setBlockAndUpdate(checking, target);
                }

                BlockEntity be = level.getBlockEntity(checking);
                if(be instanceof CommuniyCenterBlockEntity blockEntity) {
                    blockEntity.setCenter(origin);
                } else {
                    WorldmarketplaceMod.LOGGER.warn("Failed to place Community Center Block Entity at {}", checking);
                }
            }
        }
    }
}
