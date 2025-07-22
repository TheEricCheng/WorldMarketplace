package com.awwwsl.worldmarketplace.blocks;

import com.awwwsl.worldmarketplace.MarketSavedData;
import com.awwwsl.worldmarketplace.ModConfig;
import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import com.awwwsl.worldmarketplace.api.Market;
import com.awwwsl.worldmarketplace.display.InboxMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Objects;

public class InboxBlockEntity extends BlockEntity implements MenuProvider {
    private BigDecimal value;
    private ChunkPos center;

    public InboxBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(WorldmarketplaceMod.INBOX_BLOCK_ENTITY.get(), blockPos, blockState);
        value = BigDecimal.ZERO;
    }

    @Override
    public void load(@NotNull CompoundTag compoundTag) {
        var valueStr = compoundTag.getString("value");
        var centerX = compoundTag.getInt("centerX");
        var centerZ = compoundTag.getInt("centerZ");
        this.value = new BigDecimal(valueStr);
        this.center = new ChunkPos(centerX, centerZ);
        super.load(compoundTag);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag compoundTag) {
        compoundTag.putString("value", value.toString());
        compoundTag.putInt("centerX", center.x);
        compoundTag.putInt("centerZ", center.z);
        super.saveAdditional(compoundTag);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("Inbox");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        return new InboxMenu(containerId, inventory, Objects.requireNonNull(this.getMarket()));
    }

    public @Nullable Market getMarket() {
        if(this.level instanceof ServerLevel serverLevel) {
            return MarketSavedData.get(serverLevel).getMarket(serverLevel, this.center);
        } else {
            throw new RuntimeException("ShipmentBoxBlockEntity::getMarket can only be called on server side");
        }
    }

    // same as ShipmentBoxBlockEntity::generateMarket
    public void generateMarket(ServerLevel level, StructureStart start) {
        if (start == StructureStart.INVALID_START) {
            throw new IllegalArgumentException("Invalid structure start provided");
        }
        var worldDefaults = ModConfig.getWorldMarket(level.getServer());
        var defaults = worldDefaults.markets();
        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var optFirst = defaults.stream().filter(
            e -> e.location().equals(registry.getKey(start.getStructure()))
        ).findFirst();
        if (optFirst.isPresent()) {
            var first = optFirst.orElseThrow();
            var savedData = MarketSavedData.get(level);
            var market = savedData.getMarket(level, start.getChunkPos());
            if (market == null) {
                var itemRegistry = level.getServer().registryAccess().registryOrThrow(Registries.ITEM);
                market = Market.newFromDefaultsFilter(first, marketItem -> {
                    if (itemRegistry.getOptional(marketItem.id).isEmpty()) {
                        WorldmarketplaceMod.LOGGER.warn("Market item {} not found in registry, skipping", marketItem.id);
                        return false; // Skip items not found in registry
                    }
                    return true;
                });
                savedData.saveMarket(level, start.getChunkPos(), market);
            }
            this.center = start.getChunkPos();
        }
        setChanged();
    }

    /// StructureStart.INVALID_START for when no structure is found
    public static @NotNull StructureStart queryCenter(ServerLevel level, BlockPos blockPos) {
        var manager = level.structureManager();
        var toFetch = ModConfig.getWorldMarket(level.getServer()).markets().stream().map(market -> {
            ResourceLocation loc = market.location();
            return ResourceKey.create(Registries.STRUCTURE, loc);
        }).filter(
            key -> {
                var structures = level.getServer().registryAccess().registryOrThrow(Registries.STRUCTURE);
                if (structures.getOptional(key).isPresent()) {
                    return true;
                } else {
                    WorldmarketplaceMod.LOGGER.warn("Structure {} not found in registry, skipping", key.location());
                    return false;
                }
            }
        ).toList();
        StructureStart start = StructureStart.INVALID_START;
        for (ResourceKey<Structure> structure : toFetch) {
            start = manager.getStructureWithPieceAt(blockPos, structure);
            if (start.isValid() && start != StructureStart.INVALID_START) {
                break;
            }
        }
        return start;
    }
}
