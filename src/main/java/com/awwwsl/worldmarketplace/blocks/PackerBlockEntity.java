package com.awwwsl.worldmarketplace.blocks;

import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import com.awwwsl.worldmarketplace.items.PackageSellingItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PackerBlockEntity extends BlockEntity {
    public PackerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(WorldmarketplaceMod.PACKER_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    @NotNull
    private UUID creator = new UUID(0, 0);

    @Contract(pure = true)
    public @NotNull UUID getCreator() {
        return creator;
    }

    public void setCreator(@NotNull UUID creator) {
        this.creator = creator;
        if (this.getLevel() != null) {
            this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag compoundTag) {
        compoundTag.putUUID("creator", creator);
    }

    @Override
    public void load(@NotNull CompoundTag compoundTag) {
        super.load(compoundTag);
        if (compoundTag.hasUUID("creator")) {
            this.creator = compoundTag.getUUID("creator");
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if(side == Direction.DOWN) {
            return LazyOptional.empty();
        }
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return LazyOptional.of(() -> new PackerBlockItemHandler(this)).cast();
        }
        return LazyOptional.empty();
    }
}

class PackerBlockItemHandler implements IItemHandler {
    private final PackerBlockEntity market;

    public PackerBlockItemHandler(PackerBlockEntity market) {
        this.market = market;
    }

    @Override
    public int getSlots() {
        BlockPos below = market.getBlockPos().below();
        if (market.getLevel() == null) return 0;
        BlockEntity belowBlockEntity = market.getLevel().getBlockEntity(below);
        if(belowBlockEntity == null) return 0;
        var lazyCap = belowBlockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP);
        if(!lazyCap.isPresent()) return 0;
        var cap = lazyCap.orElseThrow(RuntimeException::new);
        return cap.getSlots();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if(stack.getItem() == WorldmarketplaceMod.PACKAGE_SELLING_ITEM.get()) {
            return stack;
        }
        if (market.getLevel() == null) return stack;
        BlockPos below = market.getBlockPos().below();
        BlockEntity belowBlockEntity = market.getLevel().getBlockEntity(below);
        if (belowBlockEntity == null) return stack;
        var lazyCap = belowBlockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP);
        if (!lazyCap.isPresent()) return stack;

        var cap = lazyCap.orElseThrow(RuntimeException::new);
        var toInsert = PackageSellingItem.fromItem(stack, market.getCreator());
        var remaining = cap.insertItem(slot, toInsert, simulate);
        if (remaining.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            return stack.split(remaining.getCount());
        }
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        BlockPos below = market.getBlockPos().below();
        if (market.getLevel() == null) return 0;
        BlockEntity belowBlockEntity = market.getLevel().getBlockEntity(below);
        if(belowBlockEntity == null) return 0;
        var lazyCap = belowBlockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP);
        if(!lazyCap.isPresent()) return 0;
        var cap = lazyCap.orElseThrow(RuntimeException::new);
        return cap.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        BlockPos below = market.getBlockPos().below();
        if (market.getLevel() == null) return false;
        BlockEntity belowBlockEntity = market.getLevel().getBlockEntity(below);
        if(belowBlockEntity == null) return false;
        var lazyCap = belowBlockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP);
        if(!lazyCap.isPresent()) return false;
        var cap = lazyCap.orElseThrow(RuntimeException::new);
        var wrap = PackageSellingItem.fromItem(stack.copy(), market.getCreator());
        return cap.isItemValid(slot, wrap);
    }
}
