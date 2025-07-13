package com.awwwsl.worldmarketplace.jade;

import com.awwwsl.worldmarketplace.blocks.ShipmentBoxBlock;
import com.awwwsl.worldmarketplace.blocks.ShipmentBoxBlockEntity;
import snownee.jade.api.*;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(ShipmentBoxBlock.BlockComponentProvider.INSTANCE, ShipmentBoxBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(ShipmentBoxBlock.BlockComponentProvider.INSTANCE, ShipmentBoxBlock.class);
    }
}

