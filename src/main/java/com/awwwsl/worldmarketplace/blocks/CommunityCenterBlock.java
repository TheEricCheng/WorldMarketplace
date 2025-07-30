package com.awwwsl.worldmarketplace.blocks;

import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommunityCenterBlock extends Block implements EntityBlock {
    public CommunityCenterBlock() {
        super(Properties.copy(Blocks.OAK_WOOD)
            .strength(3.0F));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new CommuniyCenterBlockEntity(blockPos, blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> blockBlockStateBuilder) {
        super.createBlockStateDefinition(blockBlockStateBuilder);
        blockBlockStateBuilder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext blockPlaceContext) {
        var state = super.getStateForPlacement(blockPlaceContext);
        if(state == null) state = this.defaultBlockState();
        return state.setValue(FACING, blockPlaceContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public void playerWillDestroy(@NotNull Level level, @NotNull BlockPos blockPos, @NotNull BlockState blockState, @NotNull Player player) {
        super.playerWillDestroy(level, blockPos, blockState, player);
        if(level.isClientSide) return;
        var entity = level.getBlockEntity(blockPos);
        if(entity instanceof CommuniyCenterBlockEntity centerEntity) {
            var center = centerEntity.getCenter();
            var facing = blockState.getValue(FACING);
            var right = facing.getClockWise();
            for(int dx = -1; dx <= 1; dx++) {
                for(int dy = 0; dy <= 2; dy++) {
                    BlockPos pos = center.offset(right.getStepX() * dx, dy, right.getStepZ() * dx);
                    if(pos.equals(blockPos)) continue;
                    BlockState state = level.getBlockState(pos);
                    if(state.getBlock() == WorldmarketplaceMod.COMMUNITY_CENTER_BLOCK.get()) {
                        level.destroyBlock(pos, false);
                    }
                }
            }
        }
    }
}
