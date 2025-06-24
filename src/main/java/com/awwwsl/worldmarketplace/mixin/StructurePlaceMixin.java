package com.awwwsl.worldmarketplace.mixin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureStart.class)
public class StructurePlaceMixin {
    @Inject(method = "placeInChunk", at = @At("TAIL"))
    public void placeInChunk(WorldGenLevel worldGenLevel,
                             StructureManager structureManager,
                             ChunkGenerator chunkGenerator,
                             RandomSource randomSource,
                             BoundingBox boundingBox,
                             ChunkPos chunkPos,
                             CallbackInfo ci) {
        // TODO
    }
}
