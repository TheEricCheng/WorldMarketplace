package com.awwwsl.worldmarketplace.blocks;

import com.awwwsl.worldmarketplace.MarketSavedData;
import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import com.awwwsl.worldmarketplace.api.Market;
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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.extensions.IForgeBlock;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommunityCenterBlock extends CommonHorizontalDirectionalBlock implements EntityBlock, IForgeBlock {
    public CommunityCenterBlock() {
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
    public @NotNull RenderShape getRenderShape(@NotNull BlockState p_60550_) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getLightBlock(@NotNull BlockState p_60585_, @NotNull BlockGetter p_60586_, @NotNull BlockPos p_60587_) {
        return 0;
    }


    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new CommunityCenterBlockEntity(blockPos, blockState);
    }

    @Override
    public void playerWillDestroy(@NotNull Level level, @NotNull BlockPos blockPos, @NotNull BlockState blockState, @NotNull Player player) {
        super.playerWillDestroy(level, blockPos, blockState, player);
        if(level.isClientSide) return;
        var entity = level.getBlockEntity(blockPos);
        if(entity instanceof CommunityCenterBlockEntity centerEntity) {
            var center = centerEntity.getCenter();
            var facing = blockState.getValue(FACING);
            var right = facing.getClockWise();
            for(int dx = -1; dx <= 1; dx++) {
                for(int dy = 0; dy <= 1; dy++) {
                    BlockPos pos = center.offset(right.getStepX() * dx, dy, right.getStepZ() * dx);
                    if(pos.equals(blockPos)) continue;
                    BlockState state = level.getBlockState(pos);
                    if(state.getBlock() == WorldmarketplaceMod.COMMUNITY_CENTER_BLOCK.get()) {
                        level.destroyBlock(pos, false);
                    }
                }
            }
        }
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos blockPos, @NotNull BlockState blockState, @Nullable LivingEntity placer, @NotNull ItemStack itemStack) {
        if(!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof CommunityCenterBlockEntity communityCenterBlockEntity) {
                communityCenterBlockEntity.initializeMarket(serverLevel);
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
            if(blockEntity instanceof CommunityCenterBlockEntity communityCenterBlockEntity) {
                if (communityCenterBlockEntity.getMarket() == null) {
                    player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.BLOCKS, 1.0F, 1.0F);
                    player.sendSystemMessage(Component.literal("This community center is not bind to a market"));
                }

                if(communityCenterBlockEntity.getMarket() != null) {
                    NetworkHooks.openScreen((ServerPlayer) player, communityCenterBlockEntity, buf -> {
                        buf.writeNbt(communityCenterBlockEntity.writeMarket());
                    });
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
