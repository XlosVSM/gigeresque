package mods.cybercat.gigeresque.client.entity.model;

import mod.azure.azurelib.common.api.client.model.DefaultedEntityGeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import mods.cybercat.gigeresque.Constants;
import mods.cybercat.gigeresque.client.entity.texture.EntityTextures;
import mods.cybercat.gigeresque.common.entity.impl.templebeast.RavenousTempleBeastEntity;

public class RavenousTempleBeastEntityModel extends DefaultedEntityGeoModel<RavenousTempleBeastEntity> {

    public RavenousTempleBeastEntityModel() {
        super(Constants.modResource("ravenoustemplebeast/ravenoustemplebeast"), false);
    }

    @Override
    public ResourceLocation getTextureResource(RavenousTempleBeastEntity object) {
        return object.isPassedOut() ? EntityTextures.RAVENOUSTEMPLEBEAST_STATIS : EntityTextures.RAVENOUSTEMPLEBEAST;
    }

    @Override
    public RenderType getRenderType(RavenousTempleBeastEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }

}
