package com.awwwsl.worldmarketplace.blocks;

import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class MarketTerminalBlockEntity extends MarketBlockEntity{
    public MarketTerminalBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(WorldmarketplaceMod.MARKET_TERMINAL_BLOCK_ENTITY.get(), blockPos, blockState);
    }
}
