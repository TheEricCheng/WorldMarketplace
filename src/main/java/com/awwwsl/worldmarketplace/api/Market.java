package com.awwwsl.worldmarketplace.api;

import com.awwwsl.worldmarketplace.LocalizedName;
import com.awwwsl.worldmarketplace.NamePool;
import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record Market(
        @NotNull ResourceLocation villageType,
        @NotNull LocalizedName name,
        @NotNull List<MarketItem> items
) {
    public Market(ResourceLocation villageType, LocalizedName name, List<MarketItem> items) {
        this.villageType = villageType;
        this.name = name;
        this.items = items;
    }

    public static @NotNull Market newFromDefaults(@NotNull MarketDefaults defaults, NamePool namePool, Registry<Item> itemRegistry) {
        return new Market(
            defaults.location(),
            namePool.randomName(defaults.location()),
            defaults.marketItems().stream()
                .filter(marketItemDefault -> {
                    if (itemRegistry.getOptional(marketItemDefault.item()).isEmpty()) {
                        WorldmarketplaceMod.LOGGER.warn("Market item {} not found in registry, skipping", marketItemDefault.item());
                        return false;
                    }
                    return true;
                })
                .map(e -> new MarketItem(e.item(), e.type(), e.basePrice(), e.offset()))
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
        CompoundTag nameTag = tag.getCompound("name");
        LocalizedName name = LocalizedName.load(nameTag);
        return new Market(villageType, name, items.build());
    }

    public @NotNull CompoundTag save() {
        return save(new CompoundTag());
    }

    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        tag.putString("villageType", villageType.toString());
        ListTag itemsList = new ListTag();
        for (MarketItem item : items) {
            CompoundTag itemTag = new CompoundTag();
            itemsList.add(item.save(itemTag));
        }
        tag.put("items", itemsList);

        CompoundTag nametag = new CompoundTag();
        name.save(nametag);
        tag.put("name", nametag);
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
