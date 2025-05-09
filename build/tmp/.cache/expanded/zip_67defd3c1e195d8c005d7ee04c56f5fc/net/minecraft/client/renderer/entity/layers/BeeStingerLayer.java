package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BeeStingerLayer<T extends LivingEntity, M extends PlayerModel<T>> extends StuckInBodyLayer<T, M> {
    private static final ResourceLocation BEE_STINGER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/bee/bee_stinger.png");

    public BeeStingerLayer(LivingEntityRenderer<T, M> pRenderer) {
        super(pRenderer);
    }

    @Override
    protected int numStuck(T pEntity) {
        return pEntity.getStingerCount();
    }

    @Override
    protected void renderStuckItem(
        PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, Entity pEntity, float pX, float pY, float pZ, float pPartialTick
    ) {
        float f = Mth.sqrt(pX * pX + pZ * pZ);
        float f1 = (float)(Math.atan2((double)pX, (double)pZ) * 180.0F / (float)Math.PI);
        float f2 = (float)(Math.atan2((double)pY, (double)f) * 180.0F / (float)Math.PI);
        pPoseStack.translate(0.0F, 0.0F, 0.0F);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(f1 - 90.0F));
        pPoseStack.mulPose(Axis.ZP.rotationDegrees(f2));
        float f3 = 0.0F;
        float f4 = 0.125F;
        float f5 = 0.0F;
        float f6 = 0.0625F;
        float f7 = 0.03125F;
        pPoseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        pPoseStack.scale(0.03125F, 0.03125F, 0.03125F);
        pPoseStack.translate(2.5F, 0.0F, 0.0F);
        VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entityCutoutNoCull(BEE_STINGER_LOCATION));

        for (int i = 0; i < 4; i++) {
            pPoseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            PoseStack.Pose posestack$pose = pPoseStack.last();
            vertex(vertexconsumer, posestack$pose, -4.5F, -1, 0.0F, 0.0F, pPackedLight);
            vertex(vertexconsumer, posestack$pose, 4.5F, -1, 0.125F, 0.0F, pPackedLight);
            vertex(vertexconsumer, posestack$pose, 4.5F, 1, 0.125F, 0.0625F, pPackedLight);
            vertex(vertexconsumer, posestack$pose, -4.5F, 1, 0.0F, 0.0625F, pPackedLight);
        }
    }

    private static void vertex(
        VertexConsumer pConsumer, PoseStack.Pose pPose, float pX, int pY, float pU, float pV, int pPackedLight
    ) {
        pConsumer.addVertex(pPose, pX, (float)pY, 0.0F)
            .setColor(-1)
            .setUv(pU, pV)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(pPackedLight)
            .setNormal(pPose, 0.0F, 1.0F, 0.0F);
    }
}