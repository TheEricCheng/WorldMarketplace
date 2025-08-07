package com.awwwsl.worldmarketplace.display;

import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MarketTerminalMenu extends AbstractContainerMenu {
    public MarketTerminalMenu(int containerId, Inventory playerInventory) {
        super(WorldmarketplaceMod.MARKET_TERMINAL_MENU_TYPE.get(), containerId);
    }

    public MarketTerminalMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player p_38941_, int p_38942_) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }
}
