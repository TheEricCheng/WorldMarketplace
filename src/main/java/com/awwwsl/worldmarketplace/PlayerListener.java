package com.awwwsl.worldmarketplace;

import com.awwwsl.worldmarketplace.api.PlayerVillageRepo;
import com.awwwsl.worldmarketplace.blocks.MarketBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;

public class PlayerListener {
    @SubscribeEvent
    public void onPlayerWatchChunk(ChunkWatchEvent.Watch event) {
        var player = event.getPlayer();
        ServerLevel level = player.serverLevel();
        var center = WorldmarketplaceMod.Utils.queryCenter(level, player.getOnPos());
        if(center != StructureStart.INVALID_START){
            MarketSavedData data = MarketSavedData.get(level);
            var market = data.getMarket(level, center.getChunkPos());
            if (market == null) {
                MarketBlockEntity.initializeMarket(
                    level,
                    center
                );
            }
            market = data.getMarket(level, center.getChunkPos());
            if (market == null) {
                WorldmarketplaceMod.LOGGER.error("Failed to generate market at {}", center.getChunkPos());
                return;
            }
            if(!PlayerVillageRepo.hasPlayerVisitedVillage(player, center.getChunkPos())) {
                PlayerVillageRepo.setPlayerHasVisitedVillage(player, center.getChunkPos(), true);
                var packet = new LocalizedName.Packet(market.name());
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
            }
        }
    }
}
