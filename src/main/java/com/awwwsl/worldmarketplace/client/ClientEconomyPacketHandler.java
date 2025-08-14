package com.awwwsl.worldmarketplace.client;

import com.awwwsl.worldmarketplace.api.EconomyRepo;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.math.BigDecimal;

public class ClientEconomyPacketHandler {
    public static void handle(BigDecimal balance) {
        Minecraft mc = Minecraft.getInstance();
        Player localPlayer = mc.player;
        if (localPlayer != null) {
            EconomyRepo.setBalance(localPlayer, balance);
        }
    }
}
