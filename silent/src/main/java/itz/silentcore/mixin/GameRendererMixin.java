package itz.silentcore.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import itz.silentcore.SilentCore;
import itz.silentcore.feature.event.impl.AspectRatioEvent;
import itz.silentcore.feature.event.impl.WorldRenderEvent;
import itz.silentcore.feature.module.impl.render.NoRender;
import itz.silentcore.utils.client.IMinecraft;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin implements IMinecraft {
    
    @Shadow
    private float zoom = 1.0f;
    
    @Shadow
    private float zoomX;
    
    @Shadow
    private float zoomY;
    
    @Shadow
    public float getFarPlaneDistance() {
        return 0;
    }
    
    @Inject(method = "getBasicProjectionMatrix", at = @At("TAIL"), cancellable = true)
    public void onGetBasicProjectionMatrix(float fovDegrees, CallbackInfoReturnable<Matrix4f> cir) {
        AspectRatioEvent event = new AspectRatioEvent();
        SilentCore.getInstance().eventBus.post(event);
        
        if (event.isCancelled()) {
            Matrix4f matrix4f = new Matrix4f();
            if (zoom != 1.0f) {
                matrix4f.translate(zoomX, -zoomY, 0.0f);
                matrix4f.scale(zoom, zoom, 1.0f);
            }
            matrix4f.perspective(fovDegrees * 0.01745329238474369F, event.getRatio(), 0.05f, getFarPlaneDistance());
            cir.setReturnValue(matrix4f);
        }
    }
    
    @Inject(method = "renderWorld", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD, ordinal = 0))
    public void hookWorldRender(RenderTickCounter tickCounter, CallbackInfo ci, @Local(ordinal = 2) Matrix4f matrix4f) {
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.multiplyPositionMatrix(matrix4f);
        matrixStack.translate(mc.getEntityRenderDispatcher().camera.getPos().negate());
        
        // Store matrices for projection
        itz.silentcore.utils.render.Render3D.lastWorldSpaceMatrix = matrixStack.peek();
        itz.silentcore.utils.math.Projection.lastProjMat = new Matrix4f(RenderSystem.getProjectionMatrix());
        
        WorldRenderEvent event = new WorldRenderEvent(matrixStack, tickCounter.getTickDelta(false), false);
        SilentCore.getInstance().eventBus.post(event);
        
        // Render 3D textures
        itz.silentcore.utils.render.Render3D.onWorldRender();
    }
    
    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void onTiltViewWhenHurt(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        NoRender noRender = (NoRender) SilentCore.getInstance().moduleManager.getModule("NoRender");
        if (noRender != null && noRender.isEnabled() && noRender.elements.getValueByName("Damage").isEnabled()) {
            ci.cancel();
        }
    }
}
