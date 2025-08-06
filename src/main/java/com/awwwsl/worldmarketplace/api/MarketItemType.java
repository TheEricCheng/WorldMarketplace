package com.awwwsl.worldmarketplace.api;

import com.electronwill.nightconfig.core.Config;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum MarketItemType {
    SELL,
    BUY,
    BOTH,
    ;

    public static MarketItemType fromConfig(@NotNull Config config) {
        var typeStr = config.<String>get("type");
        if (typeStr == null) {
            throw new IllegalArgumentException("MarketItemType type is null in config");
        }
        return MarketItemType.valueOf(typeStr.toUpperCase(Locale.ROOT));
    }

    public static void toConfig(@NotNull Config config, @NotNull MarketItemType type) {
        config.add("type", type.name().toLowerCase(Locale.ROOT));
    }

    public void toConfig(@NotNull Config config) {
        toConfig(config, this);
    }
}
