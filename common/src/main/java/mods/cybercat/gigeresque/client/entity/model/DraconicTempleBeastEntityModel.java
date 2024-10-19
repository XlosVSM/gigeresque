package mods.cybercat.gigeresque.client.entity.model;

import mod.azure.azurelib.common.api.client.model.DefaultedEntityGeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import mods.cybercat.gigeresque.Constants;
import mods.cybercat.gigeresque.common.entity.impl.templebeast.DraconicTempleBeastEntity;

public class DraconicTempleBeastEntityModel extends DefaultedEntityGeoModel<DraconicTempleBeastEntity> {

    public DraconicTempleBeastEntityModel() {
        super(Constants.modResource("draconictemplebeast/draconictemplebeast"), false);
    }

    @Override
    public RenderType getRenderType(DraconicTempleBeastEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }

}
