package com.awwwsl.worldmarketplace;

import com.awwwsl.worldmarketplace.api.Market;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

public class MarketSavedData extends SavedData {
    public static final String DATA_NAME = "worldmarketplace_markets";
    private final HashMap<ChunkPos, Market> markets = new HashMap<>();

    public static MarketSavedData get(ServerLevel world) {
        return world.getDataStorage().computeIfAbsent(
                MarketSavedData::load,
                MarketSavedData::new,
                DATA_NAME
        );
    }

    public Market getMarket(@NotNull ServerLevel world, @NotNull ChunkPos pos) {
        MarketSavedData data = get(world);
        return data.markets.getOrDefault(pos, null);
    }

    public void saveMarket(@NotNull ServerLevel world, @NotNull ChunkPos pos, @NotNull Market market) {
        MarketSavedData data = get(world);
        data.markets.put(pos, market);
        data.setDirty();
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        var list = new ListTag();
        for(var pos : markets.keySet()) {
            CompoundTag kv = new CompoundTag();
            kv.putInt("x", pos.x);
            kv.putInt("z", pos.z);
            CompoundTag marketTag = new CompoundTag();
            var market = markets.get(pos);
            kv.put("value", market.save(marketTag));
            list.add(kv);
        }
        tag.put("markets", list);
        return tag;
    }

    public static MarketSavedData load(CompoundTag tag) {
        var data = new MarketSavedData();
        tag.getList("markets", CompoundTag.TAG_COMPOUND).forEach(tag1 -> {
            if(tag1 instanceof CompoundTag tag2) {
                data.markets.put(new ChunkPos(tag2.getInt("x"), tag2.getInt("z")), Market.load(tag2.getCompound("value")));
            }
        });
        return data;
    }
}
