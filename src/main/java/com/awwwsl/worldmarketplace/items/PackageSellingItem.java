package com.awwwsl.worldmarketplace.items;

import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class PackageSellingItem extends Item {

    public PackageSellingItem() {
        super(new Item.Properties());
    }

    public static ItemStack fromItem(@NotNull ItemStack itemStack, UUID creator) {
        if(itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack newStack = new ItemStack(WorldmarketplaceMod.PACKAGE_SELLING_ITEM.get());
        newStack.setCount(itemStack.getCount());
        CompoundTag root = new CompoundTag();
        CompoundTag itemTag = new CompoundTag();
        var copiedStack = itemStack.copy();
        copiedStack.setCount(1);
        copiedStack.save(itemTag);
        root.put("item", itemTag);
        root.putUUID("creator", creator);
        newStack.setTag(root);
        return newStack;
    }

    public static @NotNull ItemStack getContainedItemStack(@NotNull ItemStack itemStack) {
        if(itemStack.getItem() != WorldmarketplaceMod.PACKAGE_SELLING_ITEM.get()) {
            throw new IllegalArgumentException("ItemStack is not a stack of PackageSellingItem");
        }
        var root = itemStack.getTag();
        if(root == null) {
            return ItemStack.EMPTY;
        }
        var tag = root.getCompound("item");
        if(tag.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return ItemStack.of(tag);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> components, @NotNull TooltipFlag flag) {
        try {
            var root = stack.getTag();
            if(root == null) {
                components.add(Component.literal("Invalid package item"));
                return;
            }

            // draw creator
            var creator = root.getUUID("creator");
            if(level == null || creator.equals(new UUID(0, 0))) {
                components.add(Component.literal("Unknown creator"));
            } else {
                var player = level.getPlayerByUUID(creator);
                if(player != null) {
                    components.add(Component.literal("Creator: " + player.getName().getString()));
                } else {
                    components.add(Component.literal("Creator: " + creator));
                }
            }

            // draw contained item
            var item = getContainedItemStack(stack);
            if(item.isEmpty()) {
                components.add(Component.literal("Invalid package item"));
            } else {
                components.add(Component.literal("Contains: ").append(item.getHoverName()));
            }
        } catch (Exception e) {
            components.clear();
            components.add(Component.literal("Exception during hover text rendering: ").append(e.toString()));
//            components.add(Component.literal("  ").append(Component.nullToEmpty(e.getStackTrace())));
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        var itemStack = player.getItemInHand(hand);
        if (itemStack.isEmpty() || itemStack.getItem() != WorldmarketplaceMod.PACKAGE_SELLING_ITEM.get()) {
            return InteractionResultHolder.pass(itemStack);
        }

        var item = getContainedItemStack(itemStack);
        if(player.isCrouching()) {
            item.setCount(itemStack.getCount());
            itemStack.setCount(0);
        } else {
            item.setCount(1);
            itemStack.shrink(1);
        }
        while(!item.isEmpty()) {
            var dropStack = item.split(item.getMaxStackSize());
            if(dropStack.isEmpty()) {
                break;
            }
            var drop = player.drop(dropStack, false);
            if(drop != null) {
                drop.setNoPickUpDelay();
            }
        }
        return InteractionResultHolder.success(itemStack);
    }
}
