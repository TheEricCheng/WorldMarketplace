package com.awwwsl.worldmarketplace;

import com.awwwsl.worldmarketplace.api.EconomyRepo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WorldmarketplaceMod.MOD_ID, value = Dist.CLIENT)
public class HudRenderer {
    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGuiEvent.Post event) {
        if(Minecraft.getInstance().options.renderDebug) {
            return;
        }
        var player = Minecraft.getInstance().player;
        var font = Minecraft.getInstance().font;
        if (player != null) {
            var balance = EconomyRepo.getBalance(player);
            var graphics = event.getGuiGraphics();
            graphics.drawString(font, Component.literal(WorldmarketplaceMod.DECIMAL_FORMAT.format(balance)), 10, 10, 0xFFFFFF);
        }
    }
}
