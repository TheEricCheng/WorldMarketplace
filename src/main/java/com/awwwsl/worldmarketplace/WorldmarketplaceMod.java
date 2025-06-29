package com.awwwsl.worldmarketplace;

import com.awwwsl.worldmarketplace.api.MarketItem;
import com.awwwsl.worldmarketplace.blocks.PackerBlock;
import com.awwwsl.worldmarketplace.blocks.PackerBlockEntity;
import com.awwwsl.worldmarketplace.blocks.ShipmentBoxBlock;
import com.awwwsl.worldmarketplace.blocks.ShipmentBoxBlockEntity;
import com.awwwsl.worldmarketplace.items.PackageSellingItem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.function.Supplier;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(WorldmarketplaceMod.MODID)
public class WorldmarketplaceMod {
    public static BigDecimal ECONOMY = BigDecimal.valueOf(1000);
    public static final MarketItem MARKET_ITEM = new MarketItem(BigDecimal.valueOf(10), BigDecimal.valueOf(100), BigDecimal.valueOf(0.5f));

    // Define mod id in a common place for everything to reference
    public static final String MODID = "worldmarketplace";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "worldmarketplace" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "worldmarketplace" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);

    public static final RegistryObject<Block> PACKER_BLOCK = BLOCKS.register("packer", PackerBlock::new);
    public static final RegistryObject<Item> PACKER_BLOCK_ITEM = ITEMS.register("packer", () -> new BlockItem(PACKER_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<PackerBlockEntity>> PACKER_BLOCK_ENTITY = BLOCK_ENTITIES.register("packer_block_entity", () -> BlockEntityType.Builder.of(PackerBlockEntity::new, PACKER_BLOCK.get()).build(null));

    public static final RegistryObject<Block> SHIPMENT_BOX_BLOCK = BLOCKS.register("shipment_box", ShipmentBoxBlock::new);
    public static final RegistryObject<Item> SHIPMENT_BOX_BLOCK_ITEM = ITEMS.register("shipment_box", () -> new BlockItem(SHIPMENT_BOX_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<ShipmentBoxBlockEntity>> SHIPMENT_BOX_BLOCK_ENTITY = BLOCK_ENTITIES.register("shipment_box_block_entity", () -> BlockEntityType.Builder.of(ShipmentBoxBlockEntity::new, Blocks.CHEST).build(null));

    public static final RegistryObject<Item> PACKAGE_SELLING_ITEM = ITEMS.register("package_selling_item", PackageSellingItem::new);
    public WorldmarketplaceMod() {
        //noinspection removal
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);


        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        //noinspection removal
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        if (Config.logDirtBlock) LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
