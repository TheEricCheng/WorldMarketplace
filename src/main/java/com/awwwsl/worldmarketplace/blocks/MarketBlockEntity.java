package com.awwwsl.worldmarketplace.blocks;

import com.awwwsl.worldmarketplace.MarketSavedData;
import com.awwwsl.worldmarketplace.ModConfig;
import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import com.awwwsl.worldmarketplace.api.Market;
import com.awwwsl.worldmarketplace.api.MarketDefaults;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class MarketBlockEntity extends BlockEntity {

    public @Nullable ChunkPos getMarketChunk() {
        return marketChunk;
    }

    public void setMarketChunk(@Nullable ChunkPos marketChunk) {
        this.marketChunk = marketChunk;
    }

    @Nullable
    private ChunkPos marketChunk;
    public MarketBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        if (marketChunk != null) {
            compoundTag.putInt("marketChunkX", marketChunk.x);
            compoundTag.putInt("marketChunkZ", marketChunk.z);
        }
    }

    @Override
    public void load(@NotNull CompoundTag compoundTag) {
        super.load(compoundTag);
        if(compoundTag.contains("marketChunkX") && compoundTag.contains("marketChunkZ")) {
            int x = compoundTag.getInt("marketChunkX");
            int z = compoundTag.getInt("marketChunkZ");
            this.marketChunk = new ChunkPos(x, z);
        } else {
            this.marketChunk = null; // Default to null if not present
        }
    }

    public Market getMarket() {
        if (marketChunk == null) {
            return null; // No market chunk set
        }
        var level = this.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return null; // Not in a valid server level
        }
        var marketData = MarketSavedData.get(serverLevel);
        return marketData.getMarket(serverLevel, marketChunk);
    }

    public void initializeMarket(ServerLevel level) {
        var start = WorldmarketplaceMod.Utils.queryCenter(level, getBlockPos());
        if(!tryValidateStart(start)) { return; }
        initializeMarket(level, start);
    }

    public void initializeMarket(ServerLevel level, StructureStart start) {
        validateStart(start);

        var savedData = MarketSavedData.get(level);
        var chunkPos = start.getChunkPos();

        var market = savedData.getMarket(level, chunkPos);
        if (market == null) {
            var defaultMarket = findDefaultMarket(level, start);
            if (defaultMarket == null) return;

            market = createMarketFromDefaults(level, defaultMarket);
            savedData.saveMarket(level, chunkPos, market);
        }

        this.marketChunk = chunkPos;
        setChanged();
    }

    private void validateStart(StructureStart start) {
        if (start == StructureStart.INVALID_START) {
            throw new IllegalArgumentException("Invalid structure start provided");
        }
    }
    private boolean tryValidateStart(StructureStart start) {
        if (start == StructureStart.INVALID_START) {
            return false;
        }
        return true;
    }

    private MarketDefaults findDefaultMarket(ServerLevel level, StructureStart start) {
        var worldDefaults = ModConfig.getWorldMarket(level.getServer());
        var defaults = worldDefaults.markets();

        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var key = registry.getKey(start.getStructure());

        return defaults.stream()
            .filter(e -> e.location().equals(key))
            .findFirst()
            .orElse(null);
    }

    private Market createMarketFromDefaults(ServerLevel level, MarketDefaults defaults) {
        var itemRegistry = level.getServer().registryAccess().registryOrThrow(Registries.ITEM);

        return Market.newFromDefaultsFilter(defaults, marketItem -> {
            if (itemRegistry.getOptional(marketItem.id).isEmpty()) {
                WorldmarketplaceMod.LOGGER.warn("Market item {} not found in registry, skipping", marketItem.id);
                return false;
            }
            return true;
        });
    }

    public CompoundTag writeMarket() {
        return writeMarket(new CompoundTag());
    }
    public boolean tryWriteMarket() {
        var tag = new CompoundTag();
        return tryWriteMarket(tag);
    }

    public boolean tryWriteMarket(CompoundTag tag) {
        var market = this.getMarket();
        if (market == null) {
            return false;
        }
        market.save(tag);
        return true;
    }

    public CompoundTag writeMarket(CompoundTag tag) {
        var market = this.getMarket();
        if(market == null) {
            throw new IllegalStateException("There is no market associated with this block entity");
        }
        market.save(tag);
        return tag;
    }
}
