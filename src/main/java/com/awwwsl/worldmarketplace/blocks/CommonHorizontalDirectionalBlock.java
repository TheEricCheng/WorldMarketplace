package com.awwwsl.worldmarketplace.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommonHorizontalDirectionalBlock extends HorizontalDirectionalBlock {
    public CommonHorizontalDirectionalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
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

    protected static Boolean viewBlockingNever(BlockState ignoredBlockState, BlockGetter ignoredBlockGetter, BlockPos ignoredBlockPos) {
        return false;
    }
}
