package mods.cybercat.gigeresque.mixins.common;

import mods.cybercat.gigeresque.common.util.BlockBreakProgressManager;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    @Inject(at = @At("HEAD"), method = "tick")
    public void tick(CallbackInfo callbackInfo) {
        var serverLevel = ServerLevel.class.cast(this);
        BlockBreakProgressManager.tick(serverLevel);
    }
}
