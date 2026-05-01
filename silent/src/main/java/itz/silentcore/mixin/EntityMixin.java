package itz.silentcore.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import itz.silentcore.SilentCore;
import itz.silentcore.feature.event.impl.BoundingBoxEvent;
import itz.silentcore.feature.event.impl.PlayerVelocityStrafeEvent;
import itz.silentcore.utils.client.IMinecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements IMinecraft {
    
    @Shadow
    private Box boundingBox;
    
    @Inject(method = "getBoundingBox", at = @At("HEAD"), cancellable = true)
    public void onGetBoundingBox(CallbackInfoReturnable<Box> cir) {
        BoundingBoxEvent event = new BoundingBoxEvent(boundingBox, (Entity) (Object) this);
        SilentCore.getInstance().eventBus.post(event);
        cir.setReturnValue(event.getBox());
    }
    
    /**
     * Перехватываем вычисление velocity для коррекции движения при ротации
     */
    @WrapOperation(
            method = "updateVelocity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;movementInputToVelocity(Lnet/minecraft/util/math/Vec3d;FF)Lnet/minecraft/util/math/Vec3d;")
    )
    public Vec3d hookVelocity(Vec3d movementInput, float speed, float yaw, Operation<Vec3d> original) {
        if ((Object) this == mc.player) {
            Vec3d originalVelocity = original.call(movementInput, speed, yaw);
            PlayerVelocityStrafeEvent event = new PlayerVelocityStrafeEvent(movementInput, speed, yaw, originalVelocity);
            SilentCore.getInstance().eventBus.post(event);
            return event.getVelocity();
        }
        return original.call(movementInput, speed, yaw);
    }
}
