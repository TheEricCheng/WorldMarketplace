package com.awwwsl.worldmarketplace.client;

import com.awwwsl.worldmarketplace.LocalizedName;
import com.awwwsl.worldmarketplace.api.EconomyRepo;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.checkerframework.checker.i18n.qual.Localized;

import java.math.BigDecimal;

public class ClientPacketHandler {
    public static void handleEconomyPacket(BigDecimal balance) {
        Minecraft mc = Minecraft.getInstance();
        Player localPlayer = mc.player;
        if (localPlayer != null) {
            EconomyRepo.setBalance(localPlayer, balance);
        }
    }
    public static void handleMarketNamePacket(LocalizedName name) {
        Minecraft mc = Minecraft.getInstance();
        mc.gui.setTitle(Component.literal(name.get("latin")));
    }
}
