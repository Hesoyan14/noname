package itz.silentcore.mixin;

import itz.silentcore.SilentCore;
import itz.silentcore.feature.event.impl.FogEvent;
import itz.silentcore.feature.module.impl.render.NoRender;
import itz.silentcore.utils.render.ColorRGBA;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {
    
    @Inject(method = "getFogModifier", at = @At("HEAD"), cancellable = true)
    private static void onGetFogModifier(Entity entity, float tickDelta, CallbackInfoReturnable<Object> info) {
        NoRender noRender = (NoRender) SilentCore.getInstance().moduleManager.getModule("NoRender");
        if (noRender != null && noRender.isEnabled()) {
            if (noRender.elements.getValueByName("Bad Effects").isEnabled()) {
                info.setReturnValue(null);
            }
            if (noRender.elements.getValueByName("Darkness").isEnabled() && entity instanceof LivingEntity) {
                info.setReturnValue(null);
            }
        }
    }
    
    @Inject(method = "getFogColor", at = @At("HEAD"), cancellable = true)
    private static void getFogColorHook(Camera camera, float tickDelta, ClientWorld world, int clampedViewDistance, float skyDarkness, CallbackInfoReturnable<Vector4f> cir) {
        FogEvent event = new FogEvent();
        SilentCore.getInstance().eventBus.post(event);
        if (event.isCancelled()) {
            int color = event.getColor();
            cir.setReturnValue(new Vector4f(
                ColorRGBA.redf(color), 
                ColorRGBA.greenf(color), 
                ColorRGBA.bluef(color), 
                ColorRGBA.alphaf(color)
            ));
        }
    }
    
    @Inject(method = "applyFog", at = @At("RETURN"), cancellable = true)
    private static void modifyFog(Camera camera, BackgroundRenderer.FogType fogType, Vector4f vector4f, float viewDistance, boolean thickenFog, float tickDelta, CallbackInfoReturnable<Fog> cir) {
        FogEvent event = new FogEvent();
        SilentCore.getInstance().eventBus.post(event);
        if (event.isCancelled()) {
            int color = event.getColor();
            cir.setReturnValue(new Fog(
                2.0F, 
                event.getDistance(), 
                FogShape.CYLINDER, 
                ColorRGBA.redf(color), 
                ColorRGBA.greenf(color), 
                ColorRGBA.bluef(color), 
                ColorRGBA.alphaf(color)
            ));
        }
    }
}
