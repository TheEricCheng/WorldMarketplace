package com.awwwsl.worldmarketplace.blocks;

import com.awwwsl.worldmarketplace.display.MarketMenu;
import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import com.awwwsl.worldmarketplace.api.Economy;
import com.awwwsl.worldmarketplace.api.Market;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Objects;

public class ShipmentBoxBlockEntity extends MarketBlockEntity implements MenuProvider {
    public ShipmentBoxBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(WorldmarketplaceMod.SHIPMENT_BOX_BLOCK_ENTITY.get(), blockPos, blockState);
    }
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(side != Direction.UP) {
            return LazyOptional.empty();
        }

        return cap == ForgeCapabilities.ITEM_HANDLER
                ? LazyOptional.of(() -> new ShipmentBoxItemHandler(this)).cast()
                : super.getCapability(cap, side);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("ShipmentBox");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory, @NotNull Player player) {
        return new MarketMenu(id, inventory, Objects.requireNonNull(this.getMarket()));
    }
}

class ShipmentBoxItemHandler implements IItemHandler {
    private final ShipmentBoxBlockEntity blockEntity;

    ShipmentBoxItemHandler(ShipmentBoxBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if(stack.getItem() != WorldmarketplaceMod.PACKAGE_SELLING_ITEM.get()) {
            return stack; // Only allow PackageSellingItem
        }
        Market market = blockEntity.getMarket();
        if(market == null) return stack; // No market found so cant sell

        var root = stack.getTag();
        if(root == null) return stack;

        Item item;
        ServerPlayer player;
        try {
            var itemTag = root.getCompound("item");
            if(itemTag.isEmpty()) {
                WorldmarketplaceMod.LOGGER.warn("Item tag is empty");
                return stack;
            }
            var itemStack = ItemStack.of(itemTag);
            item = itemStack.getItem();
            var creator = root.getUUID("creator");
            assert blockEntity.getLevel() != null;
            assert blockEntity.getLevel().getServer() != null;
            player = blockEntity.getLevel().getServer().getPlayerList().getPlayer(creator);

        } catch (Exception e) {
            WorldmarketplaceMod.LOGGER.warn("Failed to parse item tag", e);
            return stack; // Invalid item, return original stack
        }
        if(player == null) {
            WorldmarketplaceMod.LOGGER.warn("Player not found");
            return stack; // Player not found, return original stack
        }
        var marketItem = market.items().stream().filter(e -> e.id.equals(ForgeRegistries.ITEMS.getKey(item))).findFirst().orElse(null);
        if(marketItem == null) {
            return stack; // Item not available for selling
        }

        if(!simulate) {
            Economy.sell(player, marketItem, stack.getCount());
            player.sendSystemMessage(Component.literal("You have sold " + stack.getCount() + " " + item.getDescriptionId() + " for " + WorldmarketplaceMod.DECIMAL_FORMAT.format(marketItem.basePrice.multiply(BigDecimal.valueOf(stack.getCount()))) + " credits. Now you have " + Economy.getBalance(player) + " credits,"));
        }

        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if(stack.getItem() == WorldmarketplaceMod.PACKAGE_SELLING_ITEM.get()) {
            return true;
        } else {
            return false;
        }
    }
}
