package com.awwwsl.worldmarketplace;

import com.awwwsl.worldmarketplace.api.EconomyRepo;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.concurrent.atomic.AtomicInteger;

public class ModNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        ResourceLocation.parse(WorldmarketplaceMod.MOD_ID + ":network"),
        () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals
    );

    private static final AtomicInteger ID = new AtomicInteger(0);
    public static void register() {
        CHANNEL.registerMessage(ID.getAndIncrement(), EconomyRepo.Packet.class,
            EconomyRepo.Packet::encode,
            EconomyRepo.Packet::new,
            EconomyRepo.Packet::handle);
    }
}
