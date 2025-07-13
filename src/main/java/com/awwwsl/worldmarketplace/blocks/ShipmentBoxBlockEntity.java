package com.awwwsl.worldmarketplace.blocks;

import com.awwwsl.worldmarketplace.MarketSavedData;
import com.awwwsl.worldmarketplace.ModConfig;
import com.awwwsl.worldmarketplace.VirtualChestMenu;
import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import com.awwwsl.worldmarketplace.api.Economy;
import com.awwwsl.worldmarketplace.api.Market;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class ShipmentBoxBlockEntity extends BlockEntity implements MenuProvider {
    private ChunkPos center;

    public ShipmentBoxBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(WorldmarketplaceMod.SHIPMENT_BOX_BLOCK_ENTITY.get(), blockPos, blockState);
    }
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(side != Direction.UP) {
            return LazyOptional.empty();
        }

        return cap == ForgeCapabilities.ITEM_HANDLER
                ? LazyOptional.of(() -> new ShipmentBoxItemHandler(this)).cast()
                : super.getCapability(cap, side);
    }

    public void generateMarket(ServerLevel level, BlockPos blockPos) {
        var manager = level.structureManager();
        // TODO: read from config
        var toFetch = List.of(
                BuiltinStructures.VILLAGE_SNOWY,
                BuiltinStructures.VILLAGE_SAVANNA,
                BuiltinStructures.VILLAGE_TAIGA,
                BuiltinStructures.VILLAGE_DESERT,
                BuiltinStructures.VILLAGE_PLAINS
        );
        StructureStart start = StructureStart.INVALID_START;
        for(ResourceKey<Structure> structure : toFetch) {
            start = manager.getStructureWithPieceAt(blockPos, structure);
            if(start.isValid() && start != StructureStart.INVALID_START) {
                break;
            }
        }
        StructureStart finalStart = start;
        if(start != StructureStart.INVALID_START) {
            var worldDefaults = ModConfig.getWorldMarket();
            var defaults = worldDefaults.markets();
            Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            var optFirst = defaults.stream().filter(
                    e -> e.location().equals(registry.getKey(finalStart.getStructure()))
            ).findFirst();
            if(optFirst.isPresent()) {
                var first = optFirst.orElseThrow();
                var savedData = MarketSavedData.get(level);
                var market = savedData.getMarket(level, finalStart.getChunkPos());
                if(market == null) {
                    market = Market.newFromDefaults(first);
                    savedData.saveMarket(level, finalStart.getChunkPos(), market);
                }
                this.center = finalStart.getChunkPos();
            }
        }
        setChanged();
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("ShipmentBox");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory, @NotNull Player player) {
        return new VirtualChestMenu(id, inventory, Objects.requireNonNull(this.getMarket()));
    }

    public @Nullable Market getMarket() {
        if(this.level instanceof ServerLevel serverLevel) {
            return MarketSavedData.get(serverLevel).getMarket(serverLevel, this.center);
        } else {
            throw new RuntimeException("ShipmentBoxBlockEntity::getMarket can only be called on server side");
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        save(tag);
    }
    public CompoundTag save(@NotNull CompoundTag tag) {
        tag.putInt("chunkX", this.center.x);
        tag.putInt("chunkZ", this.center.z);
        return tag;
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.center = new ChunkPos(tag.getInt("chunkX"), tag.getInt("chunkZ"));
    }
}

class ShipmentBoxItemHandler implements IItemHandler {
    private final ShipmentBoxBlockEntity blockEntity;

    ShipmentBoxItemHandler(ShipmentBoxBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if(stack.getItem() != WorldmarketplaceMod.PACKAGE_SELLING_ITEM.get()) {
            return stack; // Only allow PackageSellingItem
        }
        Market market = blockEntity.getMarket();
        if(market == null) return stack; // No market found so cant sell

        var root = stack.getTag();
        if(root == null) return stack;

        Item item;
        ServerPlayer player;
        try {
            var itemTag = root.getString("item");
            var creator = root.getUUID("creator");
            if(itemTag.isEmpty()) {
                WorldmarketplaceMod.LOGGER.warn("Item tag is empty");
                return stack;
            }
            var loc = ResourceLocation.parse(itemTag);
            item = ForgeRegistries.ITEMS.getValue(loc);
            assert blockEntity.getLevel() != null;
            assert blockEntity.getLevel().getServer() != null;
            player = blockEntity.getLevel().getServer().getPlayerList().getPlayer(creator);

        } catch (Exception e) {
            WorldmarketplaceMod.LOGGER.warn("Failed to parse item tag", e);
            return stack; // Invalid item, return original stack
        }
        if(item == null) {
            WorldmarketplaceMod.LOGGER.warn("Item not found");
            return stack; // Item not found, return original stack
        }
        if(player == null) {
            WorldmarketplaceMod.LOGGER.warn("Player not found");
            return stack; // Player not found, return original stack
        }
        var marketItem = market.items().stream().filter(e -> e.id.equals(ForgeRegistries.ITEMS.getKey(item))).findFirst().orElse(null);
        if(marketItem == null) {
            return stack; // Item not available for selling
        }

        if(!simulate) {
            Economy.sell(player, marketItem, stack.getCount());
            player.sendSystemMessage(Component.literal("You have sold " + stack.getCount() + " " + item.getDescriptionId() + " for " + marketItem.basePrice + " credits. Now you have " + Economy.getBalance(player) + " economy,"));
        }

        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if(stack.getItem() == WorldmarketplaceMod.PACKAGE_SELLING_ITEM.get()) {
            return true;
        } else {
            return false;
        }
    }


}