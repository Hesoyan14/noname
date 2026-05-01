package itz.silentcore.mixin;

import itz.silentcore.SilentCore;
import itz.silentcore.feature.module.impl.render.NoRender;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public class InGameOverlayRendererMixin {
    
    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    private static void renderFireOverlayHook(MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        NoRender noRender = (NoRender) SilentCore.getInstance().moduleManager.getModule("NoRender");
        if (noRender != null && noRender.isEnabled() && noRender.elements.getValueByName("Fire").isEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderInWallOverlay", at = @At("HEAD"), cancellable = true)
    private static void renderInWallOverlayHook(Sprite sprite, MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        NoRender noRender = (NoRender) SilentCore.getInstance().moduleManager.getModule("NoRender");
        if (noRender != null && noRender.isEnabled() && noRender.elements.getValueByName("Block Overlay").isEnabled()) {
            ci.cancel();
        }
    }
}
