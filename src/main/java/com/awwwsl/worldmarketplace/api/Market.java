package com.awwwsl.worldmarketplace.api;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record Market(
        @NotNull ResourceLocation villageType,
        @NotNull List<MarketItem> items
) {
    public Market(ResourceLocation villageType, List<MarketItem> items) {
        this.villageType = villageType;
        this.items = items;
    }

    public static @NotNull Market newFromDefaults(@NotNull MarketDefaults defaults) {
        return new Market(
            defaults.location(),
            defaults.marketItems().stream()
                    .map(e -> new MarketItem(e.item(), e.type(), e.basePrice(), e.offset()))
                    .toList()
        );
    }

    public static @NotNull Market newFromDefaultsFilter(
            @NotNull MarketDefaults defaults,
            @NotNull java.util.function.Predicate<MarketItem> filter
    ) {
        return new Market(
            defaults.location(),
            defaults.marketItems().stream()
                    .map(e -> new MarketItem(e.item(), e.type(), e.basePrice(), e.offset()))
                    .filter(filter)
                    .toList()
        );
    }

    public static Market load(@NotNull CompoundTag tag) {
        ResourceLocation villageType = ResourceLocation.parse(tag.getString("villageType"));
        ListTag list = tag.getList("items", CompoundTag.TAG_COMPOUND);
        var items = new ImmutableList.Builder<MarketItem>();
        for (Tag item : list) {
            CompoundTag itemTag = (CompoundTag) item;
            items.add(MarketItem.load(itemTag));
        }
        return new Market(villageType, items.build());
    }

    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        tag.putString("villageType", villageType.toString());
        ListTag itemsList = new ListTag();
        for (MarketItem item : items) {
            CompoundTag itemTag = new CompoundTag();
            itemsList.add(item.save(itemTag));
        }
        tag.put("items", itemsList);
        return tag;
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public List<MarketItem> items() {
        return items;
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public ResourceLocation villageType() {
        return villageType;
    }
}
