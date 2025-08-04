package com.awwwsl.worldmarketplace.display;

import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import com.awwwsl.worldmarketplace.api.Economy;
import com.awwwsl.worldmarketplace.api.Market;
import com.awwwsl.worldmarketplace.items.ChequeItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Objects;

public class CommunityCenterMenu extends AbstractContainerMenu {
    public CommunityCenterMenu(int containerId, Inventory inv, Market market) {
        super(WorldmarketplaceMod.COMMUNITY_CENTER_MENU_TYPE.get(), containerId);

        int rows = 6; // 如果以后变成动态的也可以设置为参数
        Container container = new SimpleContainer(54);

        // 容器栏
        for (int row = 0; row < 6; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new DisplayOnlySlot(container, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        int playerInventoryY = 18 + rows * 18 + 13;

        // 玩家背包 3 行
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, playerInventoryY + row * 18));
            }
        }

        // 快捷栏
        int hotbarY = playerInventoryY + 3 * 18 + 4;
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, hotbarY));
        }
    }

    public CommunityCenterMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, Market.load(Objects.requireNonNull(data.readNbt())));
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack originalStack = slot.getItem();
        ItemStack copyStack = originalStack.copy();

        if (index >= 81) {
            // hotbar to inv
            if (!moveItemStackTo(originalStack, 54, 54 + 3 * 9, false)) {
                return ItemStack.EMPTY;
            }
        } else if(!moveItemStackTo(originalStack, 54 + 3 * 9, this.slots.size(), true)) {
            // inv to hotbar
            return ItemStack.EMPTY;
        }

        if (originalStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return copyStack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    @Override
    public void clicked(int index, int button, @NotNull ClickType clickType, @NotNull Player player) {
        if(index > 9) {
            super.clicked(index, button, clickType, player);
            return;
        }
        if(player instanceof ServerPlayer serverPlayer) {
            ItemStack clickedItem = this.getSlot(index).getItem();
            if (clickedItem.isEmpty() || !(clickedItem.getItem() instanceof ChequeItem cheque)) {
                return;
            }
            BigDecimal amount = ChequeItem.getValue(clickedItem);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                WorldmarketplaceMod.LOGGER.error("Invalid cheque amount: {}", amount);
                return;
            }

            if(!Economy.withdraw(serverPlayer, amount, true)) {
                serverPlayer.sendSystemMessage(Component.nullToEmpty("No enough balance to withdraw " + amount));
            } else {
                Economy.withdraw(serverPlayer, amount, false);
                if(!serverPlayer.getInventory().add(ChequeItem.from(amount))) {
                    var drop = serverPlayer.drop(ChequeItem.from(amount), false);
                    if(drop != null) {
                        drop.setNoPickUpDelay();
                    }
                };
            }
        }
    }
}
