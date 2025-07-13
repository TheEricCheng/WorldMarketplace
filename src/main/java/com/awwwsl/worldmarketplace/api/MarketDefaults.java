package com.awwwsl.worldmarketplace.api;

import com.electronwill.nightconfig.core.Config;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public record MarketDefaults(
        ResourceLocation location,
        List<MarketItemDefaults> marketItems
) {
    public static final MarketDefaults VILLAGE_DESERT = new MarketDefaults(
            BuiltinStructures.VILLAGE_DESERT.location(),
            List.of(
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.COD), 1, bd(58), bd(12)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.COAL), 1, bd(38), bd(6)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.SALMON), 1, bd(70), bd(15)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.IRON_INGOT), 1, bd(56), bd(7)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.RAW_GOLD), 1, bd(35), bd(5)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.OAK_LOG), 1, bd(60), bd(8)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.SAND), 1, bd(15), bd(2)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.ICE), 1, bd(27), bd(9)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.CACTUS), 1, bd(14), bd(3)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.GLASS), 2, bd(21), bd(5)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.IRON_BLOCK), 2, bd(100), bd(10)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.MUTTON), 2, bd(73), bd(7)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.COAL), 2, bd(14), bd(2)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.BONE), 2, bd(63), bd(5)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.PACKED_ICE), 2, bd(36), bd(6)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.BLUE_ICE), 3, bd(92), bd(9))
            )
    );


    public static final MarketDefaults VILLAGE_SAVANNA = new MarketDefaults(
            BuiltinStructures.VILLAGE_SAVANNA.location(),
            List.of(
//                    new MarketItemDefaults(loc("cabbage"), 1, bd(58), bd(7)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.COAL), 1, bd(49), bd(6)),
//                    new MarketItemDefaults(loc("tomato"), 1, bd(54), bd(6)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.IRON_INGOT), 1, bd(60), bd(8)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.ICE), 1, bd(21), bd(4)),
//                    new MarketItemDefaults(loc("raw_iron"), 1, bd(52), bd(6)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.ACACIA_SAPLING), 1, bd(4), bd(0)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.ACACIA_LOG), 1, bd(37), bd(4)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.GUNPOWDER), 2, bd(80), bd(10)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.BEEF), 2, bd(86), bd(7))
            )
    );

    public static final MarketDefaults VILLAGE_PLAINS = new MarketDefaults(
            BuiltinStructures.VILLAGE_PLAINS.location(),
            List.of(
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.COAL), 1, bd(43), bd(6)),
//                    new MarketItemDefaults(loc("raw_iron"), 1, bd(37), bd(4)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.COPPER_INGOT), 1, bd(40), bd(4)),
//                    new MarketItemDefaults(loc("cabbage"), 1, bd(34), bd(5)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.ANDESITE), 1, bd(20), bd(3))
//                    new MarketItemDefaults(loc("tomato"), 1, bd(36), bd(8))
            )
    );

    public static final MarketDefaults VILLAGE_SNOWY = new MarketDefaults(
            BuiltinStructures.VILLAGE_SNOWY.location(),
            List.of(
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.COAL), 1, bd(63), bd(9)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.COD), 1, bd(42), bd(6)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.CHARCOAL), 1, bd(52), bd(5)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.ICE), 1, bd(16), bd(4)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.SPRUCE_LOG), 1, bd(48), bd(4)),
//                    new MarketItemDefaults(loc("raw_zinc"), 1, bd(28), bd(4)),
//                    new MarketItemDefaults(loc("tomato"), 1, bd(48), bd(4)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.PACKED_ICE), 2, bd(25), bd(6)),
//                    new MarketItemDefaults(loc("zinc_ingot"), 2, bd(36), bd(8)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.BLUE_ICE), 3, bd(65), bd(9))
            )
    );

    public static final MarketDefaults VILLAGE_TAIGA = new MarketDefaults(
            BuiltinStructures.VILLAGE_TAIGA.location(),
            List.of(
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.WHEAT), 1, bd(53), bd(7)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.SWEET_BERRIES), 1, bd(8), bd(2)),
                    new MarketItemDefaults(ForgeRegistries.ITEMS.getKey(Items.SPRUCE_SAPLING), 1, bd(4), bd(0))
//                    new MarketItemDefaults(loc("raw_zinc"), 1, bd(32), bd(5))
//            )
    ));
    // TODO: add defaults for many mods

    public static MarketDefaults fromConfig(@NotNull Config config) {
        var name = config.<String>get("resourceLocation");
        ResourceLocation location1 = ResourceLocation.parse(name);
        List<Config> defaults = config.getOrElse("marketItems", ArrayList::new);
        List<MarketItemDefaults> items = new ArrayList<>(defaults.size());
        for (Config itemConfig : defaults) {
            items.add(MarketItemDefaults.fromConfig(itemConfig));
        }
        return new MarketDefaults(location1, items);
    }

    public void toConfig(@NotNull Config config) {
        config.set("resourceLocation", this.location().toString());
        List<Config> items = new ArrayList<>(this.marketItems.size());
        for (MarketItemDefaults item : this.marketItems) {
            Config itemConfig = Config.inMemory();
            item.toConfig(itemConfig);
            items.add(itemConfig);
        }
        config.set("marketItems", items);
    }

    private static BigDecimal bd(int i) {
        return BigDecimal.valueOf(i);
    }
}
