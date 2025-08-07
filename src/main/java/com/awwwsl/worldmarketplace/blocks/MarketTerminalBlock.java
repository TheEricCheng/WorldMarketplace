package com.awwwsl.worldmarketplace.blocks;

import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import com.awwwsl.worldmarketplace.display.MarketTerminalMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MarketTerminalBlock extends CommonHorizontalDirectionalBlock implements EntityBlock {
    public MarketTerminalBlock() {
        super(BlockBehaviour.Properties.of()
            .noCollission()
            .noOcclusion()
            .isViewBlocking(CommonHorizontalDirectionalBlock::viewBlockingNever));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext blockPlaceContext) {
        var state = this.defaultBlockState();
        return state.setValue(FACING, blockPlaceContext.getClickedFace());
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
        return new MarketTerminalBlockEntity(blockPos, blockState);
    }

    @Override
    public boolean canSurvive(@NotNull BlockState blockState, @NotNull LevelReader levelReader, @NotNull BlockPos blockPos) {
        var stickedState = levelReader.getBlockState(blockPos.relative(blockState.getValue(FACING).getOpposite()));
        return stickedState.isFaceSturdy(levelReader, blockPos.relative(blockState.getValue(FACING).getOpposite()), blockState.getValue(FACING).getOpposite());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(BlockState p_60509_, Level p_60510_, BlockPos p_60511_, Block p_60512_, BlockPos p_60513_, boolean p_60514_) {
        super.neighborChanged(p_60509_, p_60510_, p_60511_, p_60512_, p_60513_, p_60514_);
        if (!p_60510_.isClientSide) {
            if (!this.canSurvive(p_60509_, p_60510_, p_60511_)) {
                p_60510_.destroyBlock(p_60511_, true);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull InteractionResult use(@NotNull BlockState blockState,
                                          @NotNull Level level,
                                          @NotNull BlockPos blockPos,
                                          @NotNull Player player,
                                          @NotNull InteractionHand hand,
                                          @NotNull BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer && !level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof MarketTerminalBlockEntity marketTerminalBlockEntity) {
                var market = marketTerminalBlockEntity.getMarket();
                if(market != null) {
                    var provider = new MarketTerminalMenuProvider(market);
                    NetworkHooks.openScreen(serverPlayer, provider, provider::clientMenu);
                    return InteractionResult.sidedSuccess(level.isClientSide);
                } else {
                    serverPlayer.sendSystemMessage(Component.literal("This terminal is not bound to a market")); // TODO: translatable
                    return InteractionResult.FAIL;
                }
            } else {
                WorldmarketplaceMod.LOGGER.warn("MarketTerminalBlockEntity not found at position: {}", blockPos);
                return InteractionResult.FAIL;
            }
        } else {
            return InteractionResult.SUCCESS;
        }
    }
}
