package mods.cybercat.gigeresque.client.entity.model;

import mod.azure.azurelib.common.api.client.model.DefaultedEntityGeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import mods.cybercat.gigeresque.Constants;
import mods.cybercat.gigeresque.common.entity.impl.aqua.AquaticAlienEntity;

public class AquaticAlienEntityModel extends DefaultedEntityGeoModel<AquaticAlienEntity> {

    public AquaticAlienEntityModel() {
        super(Constants.modResource("aquatic_alien/aquatic_alien"), false);
    }

    @Override
    public RenderType getRenderType(AquaticAlienEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
