package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FireworkEntityRenderer extends EntityRenderer<FireworkRocketEntity> {
    private final ItemRenderer itemRenderer;

    public FireworkEntityRenderer(EntityRendererProvider.Context p_174114_) {
        super(p_174114_);
        this.itemRenderer = p_174114_.getItemRenderer();
    }

    public void render(FireworkRocketEntity p_114656_, float p_114657_, float p_114658_, PoseStack p_114659_, MultiBufferSource p_114660_, int p_114661_) {
        p_114659_.pushPose();
        p_114659_.mulPose(this.entityRenderDispatcher.cameraOrientation());
        if (p_114656_.isShotAtAngle()) {
            p_114659_.mulPose(Axis.ZP.rotationDegrees(180.0F));
            p_114659_.mulPose(Axis.YP.rotationDegrees(180.0F));
            p_114659_.mulPose(Axis.XP.rotationDegrees(90.0F));
        }

        this.itemRenderer
            .renderStatic(
                p_114656_.getItem(),
                ItemDisplayContext.GROUND,
                p_114661_,
                OverlayTexture.NO_OVERLAY,
                p_114659_,
                p_114660_,
                p_114656_.level(),
                p_114656_.getId()
            );
        p_114659_.popPose();
        super.render(p_114656_, p_114657_, p_114658_, p_114659_, p_114660_, p_114661_);
    }

    public ResourceLocation getTextureLocation(FireworkRocketEntity p_114654_) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}