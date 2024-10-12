package mods.cybercat.gigeresque.client.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.azure.azurelib.common.api.client.renderer.GeoEntityRenderer;
import mod.azure.azurelib.common.internal.common.cache.object.BakedGeoModel;
import mods.cybercat.gigeresque.client.entity.model.NeomorphAdolescentModel;
import mods.cybercat.gigeresque.common.entity.impl.neo.NeomorphAdolescentEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class NeomorphAdolescentRenderer extends GeoEntityRenderer<NeomorphAdolescentEntity> {
    public NeomorphAdolescentRenderer(EntityRendererProvider.Context context) {
        super(context, new NeomorphAdolescentModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public void preRender(PoseStack poseStack, NeomorphAdolescentEntity animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int color) {
        var scaleFactor = 1.0f + ((animatable.getGrowth() / animatable.getMaxGrowth()) / 5f);
        poseStack.scale(scaleFactor, scaleFactor, scaleFactor);
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight,
                packedOverlay, color);
    }

    @Override
    protected float getDeathMaxRotation(NeomorphAdolescentEntity entityLivingBaseIn) {
        return 0.0F;
    }

    @Override
    public float getMotionAnimThreshold(NeomorphAdolescentEntity animatable) {
        return 0.005f;
    }
}
