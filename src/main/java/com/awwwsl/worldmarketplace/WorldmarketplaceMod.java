package com.awwwsl.worldmarketplace;

import com.awwwsl.worldmarketplace.blocks.PackerBlock;
import com.awwwsl.worldmarketplace.blocks.PackerBlockEntity;
import com.awwwsl.worldmarketplace.blocks.ShipmentBoxBlock;
import com.awwwsl.worldmarketplace.blocks.ShipmentBoxBlockEntity;
import com.awwwsl.worldmarketplace.items.PackageSellingItem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.math.RoundingMode;
import java.text.DecimalFormat;

// The value here should match an entry in the META-INF/mods.toml file
@SuppressWarnings("unused")
@Mod(WorldmarketplaceMod.MOD_ID)
public class WorldmarketplaceMod {
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");
    static {
        DECIMAL_FORMAT.setRoundingMode(RoundingMode.HALF_EVEN);
    }
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "worldmarketplace";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "worldmarketplace" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    // Create a Deferred Register to hold Items which will all be registered under the "worldmarketplace" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MOD_ID);

    public static final RegistryObject<Block> PACKER_BLOCK = BLOCKS.register("packer", PackerBlock::new);
    public static final RegistryObject<Item> PACKER_BLOCK_ITEM = ITEMS.register("packer", () -> new BlockItem(PACKER_BLOCK.get(), new Item.Properties()));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<PackerBlockEntity>> PACKER_BLOCK_ENTITY = BLOCK_ENTITIES.register("packer_block_entity", () -> BlockEntityType.Builder.of(PackerBlockEntity::new, PACKER_BLOCK.get()).build(null));

    public static final RegistryObject<Block> SHIPMENT_BOX_BLOCK = BLOCKS.register("shipment_box", ShipmentBoxBlock::new);
    public static final RegistryObject<Item> SHIPMENT_BOX_BLOCK_ITEM = ITEMS.register("shipment_box", () -> new BlockItem(SHIPMENT_BOX_BLOCK.get(), new Item.Properties()));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<ShipmentBoxBlockEntity>> SHIPMENT_BOX_BLOCK_ENTITY = BLOCK_ENTITIES.register("shipment_box_block_entity", () -> BlockEntityType.Builder.of(ShipmentBoxBlockEntity::new, Blocks.CHEST).build(null));

    public static final RegistryObject<Item> PACKAGE_SELLING_ITEM = ITEMS.register("package_selling_item", PackageSellingItem::new);

    public static final RegistryObject<MenuType<VirtualChestMenu>> VIRTUAL_CHEST_MENU_TYPE = MENU_TYPES.register("virtual_chest_menu_type", () -> IForgeMenuType.create(VirtualChestMenu::new));
    public WorldmarketplaceMod() {
        //noinspection removal
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENU_TYPES.register(modEventBus);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        // ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.SERVER, ModConfig.SPEC);
        modEventBus.register(this);
    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        MenuScreens.register(VIRTUAL_CHEST_MENU_TYPE.get(), VirtualChestScreen::new);
    }
}
