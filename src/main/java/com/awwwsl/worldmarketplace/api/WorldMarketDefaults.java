package com.awwwsl.worldmarketplace.api;

import com.electronwill.nightconfig.core.Config;

import java.util.ArrayList;
import java.util.List;

public record WorldMarketDefaults(List<MarketDefaults> markets) {

    public static WorldMarketDefaults fromConfig(Config config) {
        int version = config.getOrElse("version", 1);
        if (version > 1) {
            throw new IllegalArgumentException("Unsupported worldmarketplace.toml version: " + version);
        }
        List<Config> defaults = config.getOrElse("markets", ArrayList::new);
        List<MarketDefaults> markets = new ArrayList<>(defaults.size());
        for (Config marketConfig : defaults) {
            markets.add(MarketDefaults.fromConfig(marketConfig));
        }
        return new WorldMarketDefaults(markets);
    }

    public void toConfig(Config config) {
        config.set("version", 1);
        List<Config> markets = new ArrayList<>(this.markets.size());
        for (MarketDefaults market : this.markets) {
            Config marketConfig = Config.inMemory();
            market.toConfig(marketConfig);
            markets.add(marketConfig);
        }
        config.set("markets", markets);
    }

    public static WorldMarketDefaults DEFAULT = new WorldMarketDefaults(List.of(
//            MarketDefaults.VILLAGE_PLAINS,
//            MarketDefaults.VILLAGE_DESERT,
//            MarketDefaults.VILLAGE_SAVANNA,
//            MarketDefaults.VILLAGE_SNOWY,
//            MarketDefaults.VILLAGE_TAIGA
    ));
}
