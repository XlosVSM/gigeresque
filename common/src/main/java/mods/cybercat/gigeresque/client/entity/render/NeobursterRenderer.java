package mods.cybercat.gigeresque.client.entity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.azure.azurelib.common.api.client.renderer.GeoEntityRenderer;
import mod.azure.azurelib.common.internal.common.cache.object.BakedGeoModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import mods.cybercat.gigeresque.client.entity.model.NeobursterModel;
import mods.cybercat.gigeresque.common.entity.impl.neo.NeobursterEntity;

public class NeobursterRenderer extends GeoEntityRenderer<NeobursterEntity> {

    public NeobursterRenderer(EntityRendererProvider.Context context) {
        super(context, new NeobursterModel());
        this.shadowRadius = 0.25f;
    }

    @Override
    public void preRender(
        PoseStack poseStack,
        NeobursterEntity animatable,
        BakedGeoModel model,
        MultiBufferSource bufferSource,
        VertexConsumer buffer,
        boolean isReRender,
        float partialTick,
        int packedLight,
        int packedOverlay,
        int color
    ) {
        var scaleFactor = 1.0f + ((animatable.getGrowth() / animatable.getMaxGrowth()) / 5f);
        poseStack.scale(scaleFactor, scaleFactor, scaleFactor);
        super.preRender(
            poseStack,
            animatable,
            model,
            bufferSource,
            buffer,
            isReRender,
            partialTick,
            packedLight,
            packedOverlay,
            color
        );
    }

    @Override
    protected float getDeathMaxRotation(NeobursterEntity entityLivingBaseIn) {
        return 0.0F;
    }
}
