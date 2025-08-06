package com.awwwsl.worldmarketplace;

import com.awwwsl.worldmarketplace.blocks.*;
import com.awwwsl.worldmarketplace.display.ChequeMachineMenu;
import com.awwwsl.worldmarketplace.display.CommunityCenterMenu;
import com.awwwsl.worldmarketplace.display.InboxMenu;
import com.awwwsl.worldmarketplace.display.ShipmentBoxMenu;
import com.awwwsl.worldmarketplace.items.ChequeItem;
import com.awwwsl.worldmarketplace.items.CommunityCenterBlockItem;
import com.awwwsl.worldmarketplace.items.PackageSellingItem;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Optional;

// The value here should match an entry in the META-INF/mods.toml file
@SuppressWarnings("unused")
@Mod(WorldmarketplaceMod.MOD_ID)
public class WorldmarketplaceMod {
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");
    public static final MathContext MATH_CONTEXT = new MathContext(4, RoundingMode.HALF_EVEN);

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
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final RegistryObject<Block> PACKER_BLOCK = BLOCKS.register("packer", PackerBlock::new);
    public static final RegistryObject<Item> PACKER_BLOCK_ITEM = ITEMS.register("packer", () -> new BlockItem(PACKER_BLOCK.get(), new Item.Properties()));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<PackerBlockEntity>> PACKER_BLOCK_ENTITY = BLOCK_ENTITIES.register("packer_block_entity", () -> BlockEntityType.Builder.of(PackerBlockEntity::new, PACKER_BLOCK.get()).build(null));

    public static final RegistryObject<Block> SHIPMENT_BOX_BLOCK = BLOCKS.register("shipment_box", ShipmentBoxBlock::new);
    public static final RegistryObject<Item> SHIPMENT_BOX_BLOCK_ITEM = ITEMS.register("shipment_box", () -> new BlockItem(SHIPMENT_BOX_BLOCK.get(), new Item.Properties()));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<ShipmentBoxBlockEntity>> SHIPMENT_BOX_BLOCK_ENTITY = BLOCK_ENTITIES.register("shipment_box_block_entity", () -> BlockEntityType.Builder.of(ShipmentBoxBlockEntity::new, Blocks.CHEST).build(null));

    public static final RegistryObject<Block> CHEQUE_MACHINE_BLOCK = BLOCKS.register("cheque_machine", ChequeMachineBlock::new);
    public static final RegistryObject<Item> CHEQUE_MACHINE_BLOCK_ITEM = ITEMS.register("cheque_machine", () -> new BlockItem(CHEQUE_MACHINE_BLOCK.get(), new Item.Properties()));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<ChequeMachineBlockEntity>> CHEQUE_MACHINE_BLOCK_ENTITY = BLOCK_ENTITIES.register("cheque_machine_block_entity", () -> BlockEntityType.Builder.of(ChequeMachineBlockEntity::new, CHEQUE_MACHINE_BLOCK.get()).build(null));

    public static final RegistryObject<Block> INBOX_BLOCK = BLOCKS.register("inbox", InboxBlock::new);
    public static final RegistryObject<Item> INBOX_BLOCK_ITEM = ITEMS.register("inbox", () -> new BlockItem(INBOX_BLOCK.get(), new Item.Properties()));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<InboxBlockEntity>> INBOX_BLOCK_ENTITY = BLOCK_ENTITIES.register("inbox_block_entity", () -> BlockEntityType.Builder.of(InboxBlockEntity::new, INBOX_BLOCK.get()).build(null));

    public static final RegistryObject<Block> COMMUNITY_CENTER_BLOCK = BLOCKS.register("community_center", CommunityCenterBlock::new);
    public static final RegistryObject<Item> COMMUNITY_CENTER_BLOCK_ITEM = ITEMS.register("community_center", CommunityCenterBlockItem::new);
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<CommunityCenterBlockEntity>> COMMUNITY_CENTER_BLOCK_ENTITY = BLOCK_ENTITIES.register("community_center_block_entity", () -> BlockEntityType.Builder.of(CommunityCenterBlockEntity::new, COMMUNITY_CENTER_BLOCK.get()).build(null));

    public static final RegistryObject<Item> PACKAGE_SELLING_ITEM = ITEMS.register("package_selling_item", PackageSellingItem::new);
    public static final RegistryObject<Item> CHEQUE_ITEM = ITEMS.register("cheque", ChequeItem::new);

    public static final RegistryObject<MenuType<ShipmentBoxMenu>> SHIPMENT_BOX_MENU_TYPE = MENU_TYPES.register("shipment_box_menu_type", () -> IForgeMenuType.create(ShipmentBoxMenu::new));
    public static final RegistryObject<MenuType<ChequeMachineMenu>> CHEQUE_MACHINE_MENU_TYPE = MENU_TYPES.register("cheque_machine_menu_type", () -> IForgeMenuType.create(ChequeMachineMenu::new));
    public static final RegistryObject<MenuType<InboxMenu>> INBOX_MENU_TYPE = MENU_TYPES.register("inbox_menu_type", () -> IForgeMenuType.create(InboxMenu::new));
    public static final RegistryObject<MenuType<CommunityCenterMenu>> COMMUNITY_CENTER_MENU_TYPE = MENU_TYPES.register("community_center_menu_type", () -> IForgeMenuType.create(CommunityCenterMenu::new));

    public static final RegistryObject<CreativeModeTab> MOD_CREATIVE_MODE_TAB = CREATIVE_MODE_TABS.register("creative_tab", () ->
            CreativeModeTab.builder()
                    .title(Component.literal("WorldMarketplace"))
                    .icon(() -> new ItemStack(PACKAGE_SELLING_ITEM.get()))
                    .displayItems((parameters, output) -> {
                        ITEMS.getEntries().forEach(entry -> {
                            output.accept(entry.get());
                        });
                    })
                    .build()
    );

    public WorldmarketplaceMod() {
        @SuppressWarnings("removal") IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.register(this);

        ModNetwork.register();
    }

    public static class Utils {
        @SuppressWarnings("deprecation")
        public static @NotNull StructureStart queryCenter(ServerLevel level, BlockPos blockPos) {
            var manager = level.structureManager();
            var registry = level.getServer().registryAccess().registryOrThrow(Registries.STRUCTURE);
            var toFetch = ModConfig.getWorldMarket(level.getServer()).markets().stream().map(market -> {
                ResourceLocation loc = market.location();
                return ResourceKey.create(Registries.STRUCTURE, loc);
            }).map(
                key -> {
                    var optional = registry.getOptional(key);
                    if(optional.isPresent()) {
                        return optional;
                    } else {
                        WorldmarketplaceMod.LOGGER.warn("Structure {} not found in registry, skipping", key.location());
                        return Optional.<Structure>empty();
                    }
                }
            ).flatMap(Optional::stream).toList();
            StructureStart start = StructureStart.INVALID_START;
            for (Structure structure : toFetch) {
                BoundingBox aabb = null;

                for(StructureStart possibleStart : manager.startsForStructure(SectionPos.of(blockPos), structure)){
                    if(possibleStart == StructureStart.INVALID_START) continue; // what
                    for(StructurePiece piece : possibleStart.getPieces()) {
                        if(aabb == null){
                            aabb = piece.getBoundingBox();
                        }

                        aabb.encapsulate(piece.getBoundingBox());
                    }
                    assert aabb != null;
//                    aabb = scaleBoundingBox(aabb, 0.4, 3);
                    WorldmarketplaceMod.LOGGER.debug("Structure {} found at {}, AABB: {}", structure, possibleStart.getChunkPos(), aabb);
                    if(aabb.isInside(blockPos)){
                        start = possibleStart;
                        break;
                    }
                }
            }
            return start;
        }

        private static BoundingBox scaleBoundingBox(BoundingBox box, double xzScale, int yExpand) {
            int centerX = (box.minX() + box.maxX()) / 2;
            int centerZ = (box.minZ() + box.maxZ()) / 2;

            int halfX = (int) Math.ceil((box.getXSpan() * xzScale) / 2.0);
            int halfZ = (int) Math.ceil((box.getZSpan() * xzScale) / 2.0);

            int minX = centerX - halfX;
            int maxX = centerX + halfX;
            int minZ = centerZ - halfZ;
            int maxZ = centerZ + halfZ;

            int minY = box.minY() - yExpand;
            int maxY = box.maxY() + yExpand;

            return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }
}
