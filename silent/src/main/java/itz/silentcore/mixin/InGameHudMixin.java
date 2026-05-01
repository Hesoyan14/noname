package itz.silentcore.mixin;

import itz.silentcore.SilentCore;
import itz.silentcore.feature.event.impl.Render2DEvent;
import itz.silentcore.feature.module.impl.render.CrossHair;
import itz.silentcore.utils.render.RenderContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void render(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        new Render2DEvent(new RenderContext(context)).hook();
    }
    
    @Inject(method = "renderCrosshair", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;CROSSHAIR_TEXTURE:Lnet/minecraft/util/Identifier;"), cancellable = true)
    public void onRenderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        CrossHair crossHair = (CrossHair) SilentCore.getInstance().moduleManager.getModule("CrossHair");
        if (crossHair != null && crossHair.isEnabled()) {
            crossHair.onRenderCrossHair();
            ci.cancel();
        }
    }
}