package com.awwwsl.worldmarketplace.blocks;

import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class ShipmentBoxBlock extends Block implements EntityBlock {
    public ShipmentBoxBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .sound(SoundType.WOOD)
        );
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new ShipmentBoxBlockEntity(blockPos, blockState);
    }

    @Override
    public void setPlacedBy(@NotNull Level level,
                            @NotNull BlockPos blockPos,
                            @NotNull BlockState blockState,
                            @Nullable LivingEntity livingEntity,
                            @NotNull ItemStack itemStack) {
        if (!level.isClientSide && livingEntity != null) {
            ServerLevel serverLevel = (ServerLevel) level;
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof ShipmentBoxBlockEntity shipmentBoxBlockEntity) {
                shipmentBoxBlockEntity.generateMarket(serverLevel, blockPos);
            }
        }
    }


    @SuppressWarnings("deprecation")
    @Override
    public @NotNull InteractionResult use(@NotNull BlockState blockState,
                                          Level level,
                                          @NotNull BlockPos blockPos,
                                          @NotNull Player player,
                                          @NotNull InteractionHand hand,
                                          @NotNull BlockHitResult hitResult) {
        if(!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if(blockEntity instanceof ShipmentBoxBlockEntity shipmentBoxBlockEntity) {
                if (shipmentBoxBlockEntity.getMarket() == null) {
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.BLOCKS, 1.0F, 1.0F);
                    player.sendSystemMessage(Component.literal("This shipment box is not bind to a market"));
                }

                if(shipmentBoxBlockEntity.getMarket() != null) {
                    NetworkHooks.openScreen((ServerPlayer) player, shipmentBoxBlockEntity, buf -> {
                        var tag = new CompoundTag();
                        buf.writeNbt(shipmentBoxBlockEntity.save(tag));
                    });
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }


    public enum BlockComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor blockAccessor, IPluginConfig pluginConfig) {
            try {
                var tag = blockAccessor.getServerData();
                var villageType = tag.getString("villageType");
                if(villageType.isEmpty()) {
                    tooltip.add(Component.translatable("tooltip.worldmarketplace.shipment_box.not_bound"));
                } else {
//                    tooltip.add(Component.translatable("tooltip.worldmarketplace.shipment_box.price", tag.getDouble("price")).append(getVillageTypeTooltip(villageType)));
                    tooltip.add(getVillageTypeTooltip(villageType));
                }
            } catch (Exception ex) {
                tooltip.add(Component.translatable("tooltip.worldmarketplace.shipment_box.error", ex.getMessage()));
            }
        }

        private MutableComponent getVillageTypeTooltip(String villageType) {
            StringBuilder builder = new StringBuilder("structure.");
            var rl = ResourceLocation.parse(villageType);
            builder.append(rl.getNamespace());
            builder.append(".");
            builder.append(rl.getPath());
            return Component.translatable(builder.toString());
        }

        @Override
        public ResourceLocation getUid() {
            return ResourceLocation.parse(WorldmarketplaceMod.MOD_ID + ":" + "block_component_provider");
        }

        @Override
        public void appendServerData(CompoundTag compoundTag, BlockAccessor blockAccessor) {
            var be = (ShipmentBoxBlockEntity) blockAccessor.getBlockEntity();
            if (be != null) {
                var market = be.getMarket();
                if(market != null) {
                    compoundTag.putString("villageType", market.villageType().toString());
                }
            }
        }
    }

}
