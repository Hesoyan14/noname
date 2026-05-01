package itz.silentcore.utils.rotation;

import itz.silentcore.utils.client.IMinecraft;
import lombok.Getter;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationManager implements IMinecraft {
    private static final RotationManager INSTANCE = new RotationManager();
    
    @Getter
    private Rotation currentRotation = null;
    @Getter
    private Rotation targetRotation = null;
    @Getter
    private Rotation serverRotation = null;
    
    private float smoothSpeed = 0.3f;
    private boolean smooth = true;
    private boolean clientSide = true; // Не крутить камеру игрока

    public static RotationManager getInstance() {
        return INSTANCE;
    }

    public void setTargetRotation(Rotation rotation, boolean smooth) {
        this.targetRotation = rotation;
        this.smooth = smooth;
        
        if (!smooth) {
            this.currentRotation = rotation.copy();
            this.serverRotation = rotation.copy();
        }
    }

    public void setTargetRotation(Vec3d targetPos, boolean smooth) {
        if (mc.player == null) return;
        
        Vec3d playerEyePos = mc.player.getEyePos();
        Vec3d direction = targetPos.subtract(playerEyePos);
        
        Rotation rotation = Rotation.fromVec3d(direction);
        setTargetRotation(rotation, smooth);
    }

    public void update() {
        if (mc.player == null) return;
        
        if (targetRotation == null) {
            currentRotation = null;
            serverRotation = null;
            return;
        }

        if (currentRotation == null) {
            currentRotation = new Rotation(mc.player.getYaw(), mc.player.getPitch());
            serverRotation = currentRotation.copy();
        }

        if (smooth) {
            // Плавная интерполяция
            float yawDiff = MathHelper.wrapDegrees(targetRotation.getYaw() - currentRotation.getYaw());
            float pitchDiff = targetRotation.getPitch() - currentRotation.getPitch();
            
            currentRotation.setYaw(currentRotation.getYaw() + yawDiff * smoothSpeed);
            currentRotation.setPitch(currentRotation.getPitch() + pitchDiff * smoothSpeed);
        } else {
            // Мгновенная ротация
            currentRotation = targetRotation.copy();
        }

        serverRotation = currentRotation.copy();
        
        // НЕ применяем ротацию к камере игрока для client-side режима
        // Ротация будет применена только в пакетах движения
    }

    public void reset() {
        currentRotation = null;
        targetRotation = null;
        serverRotation = null;
    }

    public boolean isRotating() {
        return currentRotation != null;
    }
    
    public Rotation getRotationForPacket() {
        return serverRotation != null ? serverRotation : new Rotation(mc.player.getYaw(), mc.player.getPitch());
    }
}
