package itz.silentcore.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import itz.silentcore.utils.rotation.Turns;
import itz.silentcore.utils.rotation.TurnsConnection;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    
    private double prevX = 0.0;
    private double prevZ = 0.0;
    private float prevBodyYaw = 0.0f;
    private boolean initialized = false;
    
    /**
     * Применяем серверную ротацию к yaw в пакетах движения
     * Это позволяет делать silent rotations (камера не двигается, но сервер видит поворот)
     */
    @ModifyExpressionValue(
            method = {"sendMovementPackets", "tick"}, 
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getYaw()F")
    )
    private float hookSilentRotationYaw(float original) {
        Turns rotation = TurnsConnection.INSTANCE.getCurrentAngle();
        if (rotation != null) {
            ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
            
            // Инициализация при первом вызове
            if (!initialized) {
                prevX = player.getX();
                prevZ = player.getZ();
                prevBodyYaw = player.getBodyYaw();
                initialized = true;
            }
            
            float currentYaw = rotation.getYaw();
            
            // Вычисляем bodyYaw для правильной анимации тела
            float newBodyYaw = calculateBodyYaw(
                currentYaw,
                prevBodyYaw,
                prevX,
                prevZ,
                player.getX(),
                player.getZ(),
                player.handSwingProgress
            );
            
            prevBodyYaw = newBodyYaw;
            prevX = player.getX();
            prevZ = player.getZ();
            
            // Устанавливаем bodyYaw и headYaw для визуального отображения
            player.setBodyYaw(newBodyYaw);
            player.setHeadYaw(currentYaw);
            
            return currentYaw;
        }
        return original;
    }

    /**
     * Применяем серверную ротацию к pitch в пакетах движения
     */
    @ModifyExpressionValue(
            method = {"sendMovementPackets", "tick"}, 
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getPitch()F")
    )
    private float hookSilentRotationPitch(float original) {
        Turns rotation = TurnsConnection.INSTANCE.getCurrentAngle();
        if (rotation != null) {
            return rotation.getPitch();
        }
        return original;
    }
    
    /**
     * Вычисление bodyYaw для правильной анимации тела игрока
     * Основано на Rich Client Simulations.calculateBodyYaw()
     */
    private float calculateBodyYaw(float currentYaw, float prevBodyYaw, 
                                   double prevX, double prevZ, 
                                   double currentX, double currentZ, 
                                   float handSwingProgress) {
        // Вычисляем направление движения
        double deltaX = currentX - prevX;
        double deltaZ = currentZ - prevZ;
        double movementDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        
        // Если игрок движется
        if (movementDistance > 0.0025) {
            // Вычисляем угол движения
            float movementYaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0f;
            
            // Нормализуем углы
            float yawDiff = wrapDegrees(currentYaw - prevBodyYaw);
            
            // Плавно поворачиваем тело
            float maxRotation = 75.0f;
            float clampedYawDiff = clamp(yawDiff, -maxRotation, maxRotation);
            
            float newBodyYaw = prevBodyYaw + clampedYawDiff;
            
            // Если игрок атакует, тело поворачивается к направлению взгляда
            if (handSwingProgress > 0) {
                newBodyYaw = currentYaw;
            }
            
            return wrapDegrees(newBodyYaw);
        }
        
        // Если игрок стоит на месте
        float yawDiff = wrapDegrees(currentYaw - prevBodyYaw);
        
        // Если разница большая, поворачиваем тело
        if (Math.abs(yawDiff) > 15.0f) {
            float maxRotation = 75.0f;
            float clampedYawDiff = clamp(yawDiff, -maxRotation, maxRotation);
            return wrapDegrees(prevBodyYaw + clampedYawDiff);
        }
        
        return prevBodyYaw;
    }
    
    /**
     * Нормализация угла в диапазон [-180, 180]
     */
    private float wrapDegrees(float degrees) {
        float wrapped = degrees % 360.0f;
        if (wrapped >= 180.0f) {
            wrapped -= 360.0f;
        }
        if (wrapped < -180.0f) {
            wrapped += 360.0f;
        }
        return wrapped;
    }
    
    /**
     * Ограничение значения в диапазоне
     */
    private float clamp(float value, float min, float max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}
