package com.awwwsl.worldmarketplace.blocks;

import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InboxBlock extends CommonHorizontalDirectionalBlock implements EntityBlock {

    public InboxBlock() {
        super(Properties.copy(Blocks.OAK_WOOD).noOcclusion().isViewBlocking(CommonHorizontalDirectionalBlock::viewBlockingNever));
    }

    @SuppressWarnings("deprecation")
    @Override
    public float getShadeBrightness(@NotNull BlockState p_48731_, @NotNull BlockGetter p_48732_, @NotNull BlockPos p_48733_) {
        return 1.0F;
    }

    public boolean propagatesSkylightDown(@NotNull BlockState p_48740_, @NotNull BlockGetter p_48741_, @NotNull BlockPos p_48742_) {
        return true;
    }


    @SuppressWarnings("deprecation")
    @Override
    public int getLightBlock(@NotNull BlockState p_60585_, @NotNull BlockGetter p_60586_, @NotNull BlockPos p_60587_) {
        return 0;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new InboxBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos blockPos, @NotNull BlockState blockState, @Nullable LivingEntity placer, @NotNull ItemStack itemStack) {
        if(!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof InboxBlockEntity inboxBlockEntity) {
                var center = WorldmarketplaceMod.Utils.queryCenter(serverLevel, blockPos);
                if(center != StructureStart.INVALID_START) {
                    inboxBlockEntity.initializeMarket(serverLevel, center);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull InteractionResult use(@NotNull BlockState blockState,
                                          @NotNull Level level,
                                          @NotNull BlockPos blockPos,
                                          @NotNull Player player,
                                          @NotNull InteractionHand interactionHand,
                                          @NotNull BlockHitResult blockHitResult) {
        if(!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if(blockEntity instanceof InboxBlockEntity inboxBlockEntity) {
                if (inboxBlockEntity.getMarket() == null) {
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.BLOCKS, 1.0F, 1.0F);
                    player.sendSystemMessage(Component.literal("This inbox is not bind to a market"));
                }

                if(inboxBlockEntity.getMarket() != null) {
                    NetworkHooks.openScreen((ServerPlayer) player, inboxBlockEntity, buf -> {
                        buf.writeNbt(inboxBlockEntity.writeMarket());
                    });
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);

    }
}
