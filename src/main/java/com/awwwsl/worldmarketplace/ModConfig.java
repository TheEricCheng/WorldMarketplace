package com.awwwsl.worldmarketplace;

import com.awwwsl.worldmarketplace.api.WorldMarketDefaults;
import com.electronwill.nightconfig.core.file.FileConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = WorldmarketplaceMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModConfig {
    private static final Object LOCK = new Object();
    private static volatile WorldMarketDefaults worldMarketDefaults = null;

    private static File resolveWorldConfig(MinecraftServer server) {
        Path world = server.getWorldPath(LevelResource.ROOT);
        return world.resolve("serverconfig/worldmarketplace.toml").toFile();
    }

    private static File resolveDefaultFile() {
        return FMLPaths.CONFIGDIR.get().getParent().resolve("defaultconfigs").resolve("worldmarketplace.toml").toFile();
    }

    public static @NotNull WorldMarketDefaults getWorldMarket(MinecraftServer server) {
        if (worldMarketDefaults == null) {
            synchronized (LOCK) {
                if (worldMarketDefaults == null) {
                    worldMarketDefaults = loadOrCreateConfig(resolveWorldConfig(server));
                }
            }
        }
        return worldMarketDefaults;
    }

    public static void reset() {
        synchronized (LOCK) {
            worldMarketDefaults = null;
        }
    }

    private static WorldMarketDefaults loadOrCreateConfig(File config) {
        if (!config.getParentFile().exists()) {
            config.getParentFile().mkdirs();
        }

        if (!config.exists()) {
            try {
                if (!config.createNewFile() || !config.setWritable(true)) {
                    throw new IOException("Failed to create or set writable for config file at " + config.getAbsolutePath());
                }

                WorldmarketplaceMod.LOGGER.info("Created new config at {}", config.getAbsolutePath());

                try (var nightConfig = FileConfig.builder(config).build()) {
                    WorldMarketDefaults.DEFAULT.toConfig(nightConfig);
                    nightConfig.save();
                }
            } catch (Exception e) {
                WorldmarketplaceMod.LOGGER.error("Failed to initialize config at {}", config.getAbsolutePath(), e);
                throw new RuntimeException(e);
            }
        }

        try (var nightConfig = FileConfig.builder(config).build()) {
            nightConfig.load();
            return WorldMarketDefaults.fromConfig(nightConfig);
        } catch (Exception e) {
            WorldmarketplaceMod.LOGGER.error("Failed to load config file at {}", config.getAbsolutePath(), e);
            throw new RuntimeException(e);
        }
    }

    private static void copyDefaultToWorld(MinecraftServer server) {
        var worldConfig = resolveWorldConfig(server);
        if(!worldConfig.exists()) {
            if(!worldConfig.getParentFile().exists()) {
                worldConfig.getParentFile().mkdir();
            }
            try {
                Files.copy(resolveDefaultFile().toPath(), worldConfig.toPath());
            } catch(IOException ignored) {}
        }
    }

    // Optional preload if you want
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        var defaultConfig = resolveDefaultFile();
        try {
            if (!defaultConfig.exists()) {
                if(!defaultConfig.getParentFile().exists()) {
                    defaultConfig.getParentFile().mkdir();
                }
                defaultConfig.createNewFile();
                defaultConfig.setWritable(true);

                try(var config = FileConfig.builder(defaultConfig).build()) {
                    WorldMarketDefaults.DEFAULT.toConfig(config);
                    config.save();
                }
                WorldmarketplaceMod.LOGGER.info("Created default config at {}", defaultConfig.getAbsolutePath());
            }
        } catch (Exception e) {
            WorldmarketplaceMod.LOGGER.error("Failed to create default config at {}", defaultConfig.getAbsolutePath(), e);
            throw new RuntimeException(e);
        }

        var worldConfig = resolveWorldConfig(event.getServer());
        if(!worldConfig.exists()) {
            copyDefaultToWorld(event.getServer());
        }

        reset();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        // Reset the config when the server stops to ensure fresh loading next time
        reset();
    }
}
