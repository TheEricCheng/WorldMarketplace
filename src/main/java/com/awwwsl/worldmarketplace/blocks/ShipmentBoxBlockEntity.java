package com.awwwsl.worldmarketplace.blocks;

import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

public class ShipmentBoxBlockEntity extends BlockEntity {
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
        var root = stack.getTag();
        if(root == null) return stack;

        Item item;
        ServerPlayer player;
        try {
            var itemTag = root.getString("item");
            var creator = root.getUUID("creator");
            if(itemTag.isEmpty()) {
                WorldmarketplaceMod.LOGGER.warn("Item tag is empty");
                return stack;
            }
            var loc = ResourceLocation.parse(itemTag);
            item = ForgeRegistries.ITEMS.getValue(loc);
            assert blockEntity.getLevel() != null;
            assert blockEntity.getLevel().getServer() != null;
            player = blockEntity.getLevel().getServer().getPlayerList().getPlayer(creator);
        } catch (Exception e) {
            WorldmarketplaceMod.LOGGER.warn("Failed to parse item tag", e);
            return stack; // Invalid item, return original stack
        }
        if(item == null) {
            WorldmarketplaceMod.LOGGER.warn("Item not found");
            return stack; // Item not found, return original stack
        }
        if(player == null) {
            WorldmarketplaceMod.LOGGER.warn("Player not found");
            return stack; // Player not found, return original stack
        }

        if(!simulate) {
            var price = WorldmarketplaceMod.MARKET_ITEM.getTotalPrice(BigDecimal.valueOf(stack.getCount()));
            player.sendSystemMessage(Component.literal("You have sold " + stack.getCount() + " " + item.getDescriptionId() + " for " + price.toPlainString() + " credits."));
            WorldmarketplaceMod.ECONOMY = WorldmarketplaceMod.ECONOMY.add(price);
            WorldmarketplaceMod.MARKET_ITEM.sell(BigDecimal.valueOf(stack.getCount()));
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
        if(stack.getItem() == WorldmarketplaceMod.PACKAGE_SELLING_ITEM.get()) {;
            return true;
        } else {
            return false;
        }
    }
}
