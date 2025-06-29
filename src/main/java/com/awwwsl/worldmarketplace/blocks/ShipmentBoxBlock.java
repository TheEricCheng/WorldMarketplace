package com.awwwsl.worldmarketplace.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

public class ShipmentBoxBlock extends Block implements EntityBlock {
    public ShipmentBoxBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(-1.0F, 3_600_000.0F)
                .noLootTable()
                .mapColor(MapColor.WOOD)
                .sound(SoundType.WOOD)
        );
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ShipmentBoxBlockEntity(blockPos, blockState);
    }
}
