package itz.silentcore.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import itz.silentcore.SilentCore;
import itz.silentcore.feature.module.impl.render.NoRender;
import itz.silentcore.feature.module.impl.render.WorldTweaks;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {
    
    @ModifyExpressionValue(method = "update(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;"))
    private Object injectFullBright(Object original) {
        WorldTweaks tweaks = (WorldTweaks) SilentCore.getInstance().moduleManager.getModule("WorldTweaks");
        if (tweaks != null && tweaks.isEnabled() && tweaks.settings.isEnable("Bright")) {
            return Math.max((double) original, tweaks.brightness.getCurrent() * 10);
        }
        return original;
    }
    
    @Inject(method = "getDarknessFactor", at = @At("HEAD"), cancellable = true)
    private void removeDarkness(float delta, CallbackInfoReturnable<Float> cir) {
        NoRender noRender = (NoRender) SilentCore.getInstance().moduleManager.getModule("NoRender");
        if (noRender != null && noRender.isEnabled() && noRender.elements.getValueByName("Darkness").isEnabled()) {
            cir.setReturnValue(0.0F);
        }
    }

    @Inject(method = "getDarkness", at = @At("HEAD"), cancellable = true)
    private void removeDarknessEffect(CallbackInfoReturnable<Float> cir) {
        NoRender noRender = (NoRender) SilentCore.getInstance().moduleManager.getModule("NoRender");
        if (noRender != null && noRender.isEnabled() && noRender.elements.getValueByName("Darkness").isEnabled()) {
            cir.setReturnValue(0.0F);
        }
    }

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/LightmapTextureManager;getDarknessFactor(F)F"), cancellable = true)
    private void cancelDarknessInUpdate(float delta, CallbackInfo ci) {
        NoRender noRender = (NoRender) SilentCore.getInstance().moduleManager.getModule("NoRender");
        if (noRender != null && noRender.isEnabled() && noRender.elements.getValueByName("Darkness").isEnabled()) {
            return;
        }
    }
}
