package mods.cybercat.gigeresque.client.entity.render.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mod.azure.azurelib.cache.object.GeoBone;
import mod.azure.azurelib.renderer.GeoBlockRenderer;
import mod.azure.azurelib.renderer.layer.BlockAndItemGeoLayer;
import mods.cybercat.gigeresque.client.entity.model.blocks.SarcophagusHuggerModel;
import mods.cybercat.gigeresque.common.block.entity.AlienStorageHuggerEntity;
import mods.cybercat.gigeresque.common.entity.Entities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public class SarcophagusHuggerRender extends GeoBlockRenderer<AlienStorageHuggerEntity> {
    public SarcophagusHuggerRender() {
        super(new SarcophagusHuggerModel());
        this.addRenderLayer(new BlockAndItemGeoLayer<>(this) {
            @Nullable
            @Override
            protected ItemStack getStackForBone(GeoBone bone, AlienStorageHuggerEntity animatable) {
                return switch (bone.getName()) {
                    case "item" -> new ItemStack(Items.AIR);
                    default -> null;
                };
            }

            @Override
            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, AlienStorageHuggerEntity animatable) {
                return switch (bone.getName()) {
                    default -> ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
                };
            }

            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, AlienStorageHuggerEntity animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.mulPose(Axis.ZP.rotationDegrees(0));
                poseStack.translate(0.0D, 0.0D, -2.0D);
                poseStack.scale(0.7F, 0.7F, 0.7F);
                if (animatable.checkHuggerstatus() == true)
                    Minecraft.getInstance().getEntityRenderDispatcher().render(Entities.FACEHUGGER.create(animatable.getLevel()), 0.0, 0.0, 0.0, 0.0f, partialTick, poseStack, bufferSource, packedLight);
                super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
            }
        });
    }

}