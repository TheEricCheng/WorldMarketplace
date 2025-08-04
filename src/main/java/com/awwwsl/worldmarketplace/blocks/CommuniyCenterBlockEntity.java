package com.awwwsl.worldmarketplace.blocks;

import com.awwwsl.worldmarketplace.WorldmarketplaceMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class CommuniyCenterBlockEntity extends BlockEntity {

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NotNull
    private BlockPos center;
    public CommuniyCenterBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(WorldmarketplaceMod.COMMUNITY_CENTER_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag compoundTag) {
        compoundTag.putInt("centerX", center.getX());
        compoundTag.putInt("centerY", center.getY());
        compoundTag.putInt("centerZ", center.getZ());
        super.saveAdditional(compoundTag);
    }

    @Override
    public void load(@NotNull CompoundTag compoundTag) {
        int x = compoundTag.getInt("centerX");
        int y = compoundTag.getInt("centerY");
        int z = compoundTag.getInt("centerZ");
        this.center = new BlockPos(x, y, z);
        super.load(compoundTag);
    }

    public void setCenter(@NotNull BlockPos center) {
        this.center = center;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        var compound = new CompoundTag();
        compound.putInt("centerX", center.getX());
        compound.putInt("centerY", center.getY());
        compound.putInt("centerZ", center.getZ());
        return compound;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        int x = tag.getInt("centerX");
        int y = tag.getInt("centerY");
        int z = tag.getInt("centerZ");
        this.center = new BlockPos(x, y, z);
    }

    public @NotNull BlockPos getCenter() {
        return center;
    }

    @Override
    public AABB getRenderBoundingBox() {
        // x c x
        // x x x
        // c is for center
        if(this.center.equals(this.worldPosition)) {
            return new AABB(
                this.center.getX() - 2, this.center.getY() - 2, this.center.getZ() - 2,
                this.center.getX() + 2, this.center.getY() + 1, this.center.getZ() + 2
            );
        } else {
            return new AABB(
                this.center.getX(), this.center.getY(), this.center.getZ(),
                this.center.getX(), this.center.getY(), this.center.getZ()
            );
        }
    }

    @Override
    public boolean hasCustomOutlineRendering(Player player) {
        return true;
    }

    public static class Renderer implements BlockEntityRenderer<CommuniyCenterBlockEntity> {

        private final BlockEntityRendererProvider.Context context;

        public Renderer(BlockEntityRendererProvider.Context context) {
            this.context = context;
        }

        @Override
        public boolean shouldRender(@NotNull CommuniyCenterBlockEntity blockEntity, @NotNull Vec3 cameraPos) {
            if(blockEntity.center.equals(blockEntity.worldPosition)) {
                return Vec3.atCenterOf(blockEntity.getBlockPos()).closerThan(cameraPos, this.getViewDistance());
            }
            else {
                return false;
            }
        }

        @Override
        public void render(CommuniyCenterBlockEntity be, float partialTicks, PoseStack poseStack,
                           MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
            poseStack.pushPose();

            // 可选居中
            poseStack.translate(0, 0, 0);

            BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
            BakedModel model = dispatcher.getBlockModel(be.getBlockState());

            dispatcher.getModelRenderer().renderModel(
                poseStack.last(),
                bufferSource.getBuffer(RenderType.cutout()), // 通常是 cutout 或 translucent
                be.getBlockState(),
                model,
                1.0f, 1.0f, 1.0f,
                packedLight,
                OverlayTexture.NO_OVERLAY
            );

            poseStack.popPose();
        }
    }
}
