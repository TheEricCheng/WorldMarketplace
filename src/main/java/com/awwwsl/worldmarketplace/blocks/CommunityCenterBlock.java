package com.awwwsl.worldmarketplace.blocks;

import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommunityCenterBlock extends CommonHorizontalDirectionalBlock implements EntityBlock {
    public CommunityCenterBlock() {
        super(Properties.copy(Blocks.OAK_WOOD).noOcclusion());
    }

    @Override
    public int getLightBlock(BlockState p_60585_, BlockGetter p_60586_, BlockPos p_60587_) {
        return 0;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new CommuniyCenterBlockEntity(blockPos, blockState);
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
                for(int dy = 0; dy <= 1; dy++) {
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
