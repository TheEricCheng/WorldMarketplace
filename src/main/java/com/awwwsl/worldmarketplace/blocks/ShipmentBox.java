package com.awwwsl.worldmarketplace.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class ShipmentBox extends Block {
    public ShipmentBox() {
        super(BlockBehaviour.Properties.of()
                .strength(-1.0F, 3_600_000.0F)
                .noLootTable()
                .mapColor(MapColor.WOOD)
                .sound(SoundType.WOOD)
        );
    }


}
