package com.awwwsl.worldmarketplace.display;

import com.awwwsl.worldmarketplace.api.Market;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MarketTerminalMenuProvider implements MenuProvider {
    private final Market market;

    public MarketTerminalMenuProvider(@NotNull Market market) {
        this.market = market;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("Market Terminal");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new MarketTerminalMenu(containerId, inventory, market);
    }

    public void clientMenu(FriendlyByteBuf buf) {
        buf.writeNbt(market.save());
    }
}
