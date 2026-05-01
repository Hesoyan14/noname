package itz.silentcore.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class HealthBarMixin {

    @Inject(method = "renderStatusBars", at = @At("HEAD"))
    private void onRenderStatusBarsStart(DrawContext context, CallbackInfo ci) {
        context.getMatrices().push();
        context.getMatrices().translate(0, -3, 0);
    }

    @Inject(method = "renderStatusBars", at = @At("TAIL"))
    private void onRenderStatusBarsEnd(DrawContext context, CallbackInfo ci) {
        context.getMatrices().pop();
    }
}
