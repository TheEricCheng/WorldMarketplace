package com.awwwsl.worldmarketplace.client;

import com.awwwsl.worldmarketplace.blocks.CommunityCenterBlockEntity;
import com.awwwsl.worldmarketplace.display.VirtualChestScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.awwwsl.worldmarketplace.WorldmarketplaceMod.*;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModClientInit {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MenuScreens.register(SHIPMENT_BOX_MENU_TYPE.get(), (net.minecraft.world.inventory.AbstractContainerMenu menu, Inventory inv, Component title) -> new VirtualChestScreen(menu, inv, title, 6));
        MenuScreens.register(CHEQUE_MACHINE_MENU_TYPE.get(), (net.minecraft.world.inventory.AbstractContainerMenu menu, Inventory inv, Component title) -> new VirtualChestScreen(menu, inv, title, 1));
        MenuScreens.register(INBOX_MENU_TYPE.get(), (net.minecraft.world.inventory.AbstractContainerMenu menu, Inventory inv, Component title) -> new VirtualChestScreen(menu, inv, title, 6));
        MenuScreens.register(COMMUNITY_CENTER_MENU_TYPE.get(), (net.minecraft.world.inventory.AbstractContainerMenu menu, Inventory inv, Component title) -> new VirtualChestScreen(menu, inv, title, 6));

        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(COMMUNITY_CENTER_BLOCK_ENTITY.get(), CommunityCenterBlockEntity.Renderer::new);
    }
}
