package com.awwwsl.worldmarketplace;

import com.awwwsl.worldmarketplace.api.WorldMarketDefaults;
import com.electronwill.nightconfig.core.file.FileConfig;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = WorldmarketplaceMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModConfig {
//    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
//    private static final ForgeConfigSpec.IntValue START_BALANCE = BUILDER
//            .comment("The starting balance for players when they first join the server.")
//            .translation("worldmarketplace.config.start_balance")
//            .defineInRange("startBalance", 1000, 0, Integer.MAX_VALUE);
//    public static final ForgeConfigSpec SPEC = BUILDER.build();
//
//    public static int getStartBalance() {
//        return START_BALANCE.get();
//    }
//
    private static WorldMarketDefaults worldMarketDefaults = null;

    public static @NotNull WorldMarketDefaults getWorldMarket() {
        return worldMarketDefaults;
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        var srv = event.getServer();
        var world = srv.getWorldPath(LevelResource.ROOT);
        var config = world.resolve("serverconfig/worldmarketplace.toml").toFile();
        try {
            if(!config.getParentFile().exists()) {
                config.getParentFile().mkdir();
            }
        } catch (Exception e) {
            WorldmarketplaceMod.LOGGER.error("Failed to create config directory at {}", config.getParentFile().getAbsolutePath(), e);
        }
        if(!config.exists()) {
            try {
                if(!config.createNewFile() || !config.setWritable(true)) {
                    throw new IOException("Failed to create or set writable for config file at " + config.getAbsolutePath());
                }
            } catch (IOException e) {
                WorldmarketplaceMod.LOGGER.error("Failed to create config file at {}", config.getAbsolutePath(), e);
                throw new RuntimeException(e);
            }

            WorldmarketplaceMod.LOGGER.info("Config not exists, created new one at {}", config.getAbsolutePath());
            try (var nightConfig = FileConfig.builder(config).build()) {
                WorldMarketDefaults.DEFAULT.toConfig(nightConfig);
                nightConfig.save();
            } catch(Exception e) {
                WorldmarketplaceMod.LOGGER.error("Failed to write default config at {}", config.getAbsolutePath(), e);
                throw new RuntimeException(e);
            }
        }

        try(var nightConfig = FileConfig.builder(config).build()) {
            nightConfig.load();
            worldMarketDefaults = WorldMarketDefaults.fromConfig(nightConfig);
        } catch (Exception e) {
            WorldmarketplaceMod.LOGGER.error("Failed to load config file at {}", config.getAbsolutePath(), e);
            throw new RuntimeException(e);
        }
    }
}
