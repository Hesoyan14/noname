package itz.silentcore.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import itz.silentcore.SilentCore;
import itz.silentcore.feature.event.impl.HandAnimationEvent;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    
    @WrapOperation(
        method = "renderFirstPersonItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/item/HeldItemRenderer;swingArm(FFLnet/minecraft/client/util/math/MatrixStack;ILnet/minecraft/util/Arm;)V",
            ordinal = 2
        )
    )
    private void onRenderFirstPersonItem(
        HeldItemRenderer instance,
        float swingProgress,
        float equipProgress,
        MatrixStack matrices,
        int armX,
        Arm arm,
        Operation<Void> original,
        @Local(ordinal = 0, argsOnly = true) AbstractClientPlayerEntity player,
        @Local(ordinal = 0, argsOnly = true) Hand hand
    ) {
        HandAnimationEvent event = new HandAnimationEvent(matrices, hand, swingProgress);
        SilentCore.getInstance().eventBus.post(event);
        
        if (!event.isCancelled()) {
            original.call(instance, swingProgress, equipProgress, matrices, armX, arm);
        }
    }
}
