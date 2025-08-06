package com.awwwsl.worldmarketplace.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MarketItem {
    public ResourceLocation id;
    public MarketItemType type;
    public BigDecimal basePrice;
    public BigDecimal offset;
    public LocalDateTime lastUpdate;

    public BigDecimal computed;

    public MarketItem(ResourceLocation id, MarketItemType type, BigDecimal basePrice, BigDecimal offset) {
        this.id = id;
        this.type = type;
        this.basePrice = basePrice;
        this.offset = offset;
        this.lastUpdate = LocalDateTime.now();
    }

    private MarketItem(
        ResourceLocation id,
        MarketItemType type,
        BigDecimal basePrice,
        BigDecimal offset,
        LocalDateTime lastUpdate
    ) {
        this.id = id;
        this.type = type;
        this.basePrice = basePrice;
        this.offset = offset;
        this.lastUpdate = lastUpdate;
    }

    public static @NotNull MarketItem load(@NotNull CompoundTag tag) {
        var basePrice = new BigDecimal(tag.getString("basePrice"));
        var offset = new BigDecimal(tag.getString("offset"));
        var type = MarketItemType.valueOf(tag.getString("type"));
        return new MarketItem(
            ResourceLocation.parse(tag.getString("id")),
            type,
            basePrice,
            offset,
            LocalDateTime.parse(tag.getString("lastUpdate"))
        );
    }

    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        tag.putString("id", id.toString());
        tag.putString("type", type.name());
        tag.putString("basePrice", basePrice.toString());
        tag.putString("offset", offset.toString());
        tag.putString("lastUpdate", lastUpdate.toString());
        return tag;
    }
}
