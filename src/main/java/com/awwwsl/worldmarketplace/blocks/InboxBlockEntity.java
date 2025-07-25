package com.awwwsl.worldmarketplace.blocks;

import com.awwwsl.worldmarketplace.MarketSavedData;
import com.awwwsl.worldmarketplace.ModConfig;
import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import com.awwwsl.worldmarketplace.api.Market;
import com.awwwsl.worldmarketplace.display.InboxMenu;
import com.awwwsl.worldmarketplace.items.ChequeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Objects;

public class InboxBlockEntity extends BlockEntity implements MenuProvider {
    public @NotNull BigDecimal getValue() {
        return value;
    }

    public void setValue(@NotNull BigDecimal value) {
        this.value = value;
    }

    private @NotNull BigDecimal value;
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
            return null;
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

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if(this.getMarket() == null) {
            return LazyOptional.empty();
        }
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return LazyOptional.of(() -> new InboxMenuItemHandler(this)).cast();
        }
        return LazyOptional.empty();
    }
}

class InboxMenuItemHandler implements IItemHandler {
    private final @NotNull InboxBlockEntity entity;

    public InboxMenuItemHandler(@NotNull InboxBlockEntity entity) {
        this.entity = entity;
    }

    @Override
    public int getSlots() {
        return Objects.requireNonNull(entity.getMarket()).items().size() + 1; // this wont instantiate when entity.getMarket() is null
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if(stack.getItem() != WorldmarketplaceMod.CHEQUE_ITEM.get()) {
            return stack;
        }
        var value = ChequeItem.getValue(stack).multiply(BigDecimal.valueOf(stack.getCount()));
        if(value.compareTo(BigDecimal.ZERO) < 0) {
            return stack;
        }
        if(!simulate) {
            this.entity.setValue(this.entity.getValue().add(value));
        }
        return ItemStack.EMPTY;
    }


    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        if(slot == 0) {
            return ItemStack.EMPTY; // CHEQUE ITEM
        }
        slot--;
        var marketItem = Objects.requireNonNull(entity.getMarket()).items().get(slot); // this wont instantiate when entity.getMarket() is null
        var value = entity.getValue();
        int count;
        if(value.compareTo(marketItem.basePrice.multiply(BigDecimal.valueOf(Integer.MAX_VALUE))) > 0) {
            count = Integer.MAX_VALUE;
        } else {
            count = value.divide(marketItem.basePrice, WorldmarketplaceMod.MATH_CONTEXT).intValue();
        }
        var item = ForgeRegistries.ITEMS.getValue(marketItem.id);
        if(item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        } else {
            return new ItemStack(item, count);
        }
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if(slot <= 0) {
            return ItemStack.EMPTY;
        }
        slot--;
        var marketItem = Objects.requireNonNull(entity.getMarket()).items().get(slot); // this wont instantiate when entity.getMarket() is null
        var value = entity.getValue();
        var item = ForgeRegistries.ITEMS.getValue(marketItem.id);

        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }

        BigDecimal totalCost = marketItem.basePrice.multiply(BigDecimal.valueOf(amount));
        if (value.compareTo(totalCost) < 0) {
            // Not enough money, calculate max affordable
            int maxAffordable = value.divide(marketItem.basePrice, WorldmarketplaceMod.MATH_CONTEXT).intValue();
            if (maxAffordable <= 0) {
                return ItemStack.EMPTY;
            }
//            amount = Math.min(maxAffordable, Integer.MAX_VALUE);
            amount = maxAffordable; // intValue wont throw when overflow // idea told me and idk if this is real
            totalCost = marketItem.basePrice.multiply(BigDecimal.valueOf(amount));
        }

        if (!simulate) {
            entity.setValue(value.subtract(totalCost));
        }
        return new ItemStack(item, amount);
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if(slot == 0) {
            return stack.getItem() == WorldmarketplaceMod.CHEQUE_ITEM.get();
        }
        return false;
    }
}
