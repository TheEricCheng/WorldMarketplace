package com.awwwsl.worldmarketplace;

import com.awwwsl.worldmarketplace.api.Economy;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        ResourceLocation.parse(WorldmarketplaceMod.MOD_ID + ":network"),
        () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, Economy.Packet.class,
            Economy.Packet::encode,
            Economy.Packet::new,
            Economy.Packet::handle);
    }
}
