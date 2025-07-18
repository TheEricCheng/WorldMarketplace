package com.awwwsl.worldmarketplace.display;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

public class VirtualChestScreen extends AbstractContainerScreen<AbstractContainerMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.parse("minecraft:textures/gui/container/generic_54.png");
    private final int rows;

    public VirtualChestScreen(AbstractContainerMenu menu, Inventory inv, Component title, int rows) {
        super(menu, inv, title);
        this.rows = rows;
        this.imageWidth = 176;
        this.imageHeight = 114 + rows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int containerHeight = rows * 18 + 17;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, containerHeight);
        guiGraphics.blit(TEXTURE, x, y + containerHeight, 0, 126, imageWidth, 96);
    }
}
