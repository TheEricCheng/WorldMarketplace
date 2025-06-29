package com.awwwsl.worldmarketplace.items;

import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TextComponentTagVisitor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
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
        CompoundTag tag = new CompoundTag();
        CompoundTag root = new CompoundTag();
        var item = itemStack.getItem();
        var name = ForgeRegistries.ITEMS.getKey(item);
        if(name == null) {
            WorldmarketplaceMod.LOGGER.warn("Item {} has no name for creating PackageSellingItem", item);
            return itemStack; // Invalid item, return original stack
        }
        root.putString("item", name.toString());
        root.putUUID("creator", creator);
        newStack.setTag(root);
        newStack.setCount(itemStack.getCount());
        return newStack;
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
            var loc = root.getString("item");
            if(loc.isEmpty()) {
                components.add(Component.literal("Invalid package item"));
            } else {
                var item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(loc));
                if(item == null || item instanceof AirItem) {
                    components.add(Component.literal("Invalid package item"));
                } else {
                    components.add(item.getDescription());
                }
            }
        } catch (Exception e) {
            components.add(Component.literal("Exception during hover text rendering:"));
            components.add(Component.literal("  ").append(e.getMessage()));
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        var itemStack = player.getItemInHand(hand);
        if(player.isCrouching() && !itemStack.isEmpty()) {
            var root = itemStack.getTag();
            if(root == null) {
                return InteractionResultHolder.fail(itemStack);
            }
            var loc = root.getString("item");
            if(loc.isEmpty()) {
                return InteractionResultHolder.fail(itemStack);
            }
            var item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(loc));
            if(item == null) {
                WorldmarketplaceMod.LOGGER.warn("Item {} not found for PackageSellingItem", loc);
                itemStack.shrink(1);
                return InteractionResultHolder.fail(itemStack);
            }

            var drop = player.drop(new ItemStack(item), true);
            if(drop != null) {
                drop.setNoPickUpDelay();
            }

            if(!player.isCreative()) itemStack.shrink(1);
            return InteractionResultHolder.success(itemStack);
        }
        return InteractionResultHolder.pass(itemStack);
    }
}
