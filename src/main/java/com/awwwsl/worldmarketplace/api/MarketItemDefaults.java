package com.awwwsl.worldmarketplace.api;

import com.electronwill.nightconfig.core.Config;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Objects;

public record MarketItemDefaults (
    ResourceLocation item,
    int level,
    BigDecimal basePrice,
    BigDecimal offset
) {
    public static MarketItemDefaults fromConfig(@NotNull Config config) {
        var name = config.<String>get("resourceLocation");
        Objects.requireNonNull(name);
        ResourceLocation location1 = ResourceLocation.parse(name);
        var level = config.<Integer>get("level");
        var priceStr = config.<String>get("basePrice");
        Objects.requireNonNull(priceStr);
        var basePrice = priceStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(priceStr);
        var offsetStr = config.<String>get("offset");
        Objects.requireNonNull(offsetStr);
        var offset = offsetStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(offsetStr);
        return new MarketItemDefaults(location1, level, basePrice, offset);
    }

    public void toConfig(@NotNull Config config) {
        config.add("resourceLocation", item.toString());
        config.add("level", level);
        config.add("basePrice", basePrice.toString());
        config.add("offset", offset.toString());
    }
}
