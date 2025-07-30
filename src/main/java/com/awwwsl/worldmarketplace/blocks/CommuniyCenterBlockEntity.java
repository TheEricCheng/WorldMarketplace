package com.awwwsl.worldmarketplace.blocks;

import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class CommuniyCenterBlockEntity extends BlockEntity {

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NotNull
    private BlockPos center;
    public CommuniyCenterBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(WorldmarketplaceMod.COMMUNITY_CENTER_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    public void setCenter(@NotNull BlockPos center) {
        this.center = center;
    }

    public @NotNull BlockPos getCenter() {
        return center;
    }
}
