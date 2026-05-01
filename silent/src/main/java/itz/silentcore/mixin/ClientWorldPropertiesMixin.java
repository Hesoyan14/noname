package itz.silentcore.mixin;

import itz.silentcore.SilentCore;
import itz.silentcore.feature.module.impl.render.WorldTweaks;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.Properties.class)
public class ClientWorldPropertiesMixin {
    
    @Shadow 
    private long timeOfDay;
    
    @Inject(method = "setTimeOfDay", at = @At("HEAD"), cancellable = true)
    public void setTimeOfDayHook(long timeOfDay, CallbackInfo ci) {
        WorldTweaks tweaks = (WorldTweaks) SilentCore.getInstance().moduleManager.getModule("WorldTweaks");
        if (tweaks != null && tweaks.isEnabled() && tweaks.settings.isEnable("Time")) {
            this.timeOfDay = (long) (tweaks.time.getCurrent() * 1000L);
            ci.cancel();
        }
    }
}
