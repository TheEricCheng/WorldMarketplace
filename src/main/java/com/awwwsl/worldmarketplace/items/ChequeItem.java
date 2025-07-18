package com.awwwsl.worldmarketplace.items;

import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import com.awwwsl.worldmarketplace.api.Economy;
import io.netty.util.concurrent.EventExecutorChooserFactory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;

public class ChequeItem extends Item {
    public ChequeItem() {
        super(new Item.Properties());
    }

    public static @NotNull ItemStack from(@NotNull BigDecimal value) {
        if(value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("ChequeItem.from: Value must be greater than zero");
        }
        var item = new ItemStack(WorldmarketplaceMod.CHEQUE_ITEM.get());
        var tag = new CompoundTag();
        tag.putString("value", value.toString());
        item.setTag(tag);
        return item;
    }

    public static void setValue(@NotNull ItemStack itemStack, @NotNull BigDecimal value) {
        if(itemStack.getItem() != WorldmarketplaceMod.CHEQUE_ITEM.get()) {
            throw new IllegalArgumentException("ItemStack is not a ChequeItem");
        }
        if(value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("ChequeItem.setValue: Value must be greater than zero");
        }
        var tag = itemStack.getOrCreateTag();
        tag.putString("value", value.toString());
        itemStack.setTag(tag);
        return;
    }

    public static @NotNull BigDecimal getValue(@NotNull ItemStack itemStack) {
        if(itemStack.getItem() != WorldmarketplaceMod.CHEQUE_ITEM.get()) {
            throw new IllegalArgumentException("ItemStack is not a ChequeItem");
        }
        var tag = itemStack.getTag();
        if(tag == null || !tag.contains("value")) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(tag.getString("value"));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand interactionHand) {
        var item = player.getItemInHand(interactionHand);
        if(!player.isCrouching() || item.isEmpty() || item.getItem() != WorldmarketplaceMod.CHEQUE_ITEM.get()) {
            return InteractionResultHolder.pass(item);
        }

        if(level.isClientSide) {
            return InteractionResultHolder.success(item);
        }
        var serverPlayer = (ServerPlayer)player;
        var value = getValue(item);
        Economy.setBalance(serverPlayer, Economy.getBalance(serverPlayer).add(value));
        item.shrink(1);
        return InteractionResultHolder.success(item);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack itemStack) {
        return getValue(itemStack).compareTo(BigDecimal.valueOf(100)) > 0;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level level, List<Component> components, @NotNull TooltipFlag flag) {
        components.add(Component.literal("Value: ").append(WorldmarketplaceMod.DECIMAL_FORMAT.format(getValue(itemStack))));
    }
}
