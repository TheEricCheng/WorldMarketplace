package com.awwwsl.worldmarketplace.api;

import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

public class PlayerVillageRepo {
    public static void setPlayerHasVisitedVillage(
        Player player,
        ChunkPos villagePos,
        boolean visited
    ) {
        var compound = player.getPersistentData().getCompound(WorldmarketplaceMod.PLAYER_ROOT_COMPOUND_NAME);
        ListTag list = compound.getList("visitedVillages", CompoundTag.TAG_LONG);
        if (visited) {
            list.add(LongTag.valueOf(villagePos.toLong()));
        } else {
            list.removeIf(tag -> tag instanceof LongTag longTag && longTag.getAsLong() == villagePos.toLong());
        }
        compound.put("visitedVillages", list);
    }

    public static boolean hasPlayerVisitedVillage(
        Player player,
        ChunkPos villagePos
    ) {
        var compound = player.getPersistentData().getCompound(WorldmarketplaceMod.PLAYER_ROOT_COMPOUND_NAME);
        ListTag list = compound.getList("visitedVillages", CompoundTag.TAG_LONG);
        return list.stream()
            .filter(tag -> tag instanceof LongTag)
            .map(tag -> ((LongTag) tag).getAsLong())
            .anyMatch(pos -> pos == villagePos.toLong());
    }

}
