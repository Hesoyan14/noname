package itz.silentcore.utils.rotation.impl;

import itz.silentcore.utils.math.StopWatch;
import itz.silentcore.utils.rotation.MathAngle;
import itz.silentcore.utils.rotation.RotateConstructor;
import itz.silentcore.utils.rotation.Turns;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.security.SecureRandom;

public class RWAngle extends RotateConstructor {
    private final StopWatch attackTimer = new StopWatch();
    private int count = 0;

    public RWAngle() {
        super("ReallyWorld");
    }

    @Override
    public Turns limitAngleChange(Turns currentAngle, Turns targetAngle, Vec3d vec3d, Entity entity) {
        Turns angleDelta = MathAngle.calculateDelta(currentAngle, targetAngle);
        float yawDelta = angleDelta.getYaw();
        float pitchDelta = angleDelta.getPitch();
        float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));

        float preAttackSpeed = 1;
        float postAttackSpeed = 1;
        float speed = entity != null ? preAttackSpeed : postAttackSpeed;
        
        float lineYaw = (Math.abs(yawDelta / rotationDifference) * 180);
        float linePitch = (Math.abs(pitchDelta / rotationDifference) * 180);
        
        float jitterYaw = entity != null ? 0 : (float) (-6 * Math.cos(System.currentTimeMillis() / 90D));
        float jitterPitch = entity != null ? 0 : (float) (6 * Math.sin(System.currentTimeMillis() / 90D));

        if (entity == null) {
            speed = 1F;
            jitterYaw = 0;
            jitterPitch = 0;
        }

        float moveYaw = MathHelper.clamp(yawDelta, -lineYaw, lineYaw);
        float movePitch = MathHelper.clamp(pitchDelta, -linePitch, linePitch);
        
        Turns moveAngle = new Turns(currentAngle.getYaw(), currentAngle.getPitch());
        moveAngle.setYaw(MathHelper.lerp(randomLerp(speed, speed + 0.2F), currentAngle.getYaw(), currentAngle.getYaw() + moveYaw) + jitterYaw);
        moveAngle.setPitch(MathHelper.lerp(randomLerp(speed, speed + 0.2F), currentAngle.getPitch(), currentAngle.getPitch() + movePitch) + jitterPitch);

        if (count > 0 && count % 50 == 0 && !attackTimer.finished(200)) {
            moveAngle.setPitch(MathHelper.lerp(0.55F, currentAngle.getPitch(), currentAngle.getPitch() - 90) + jitterPitch);
        }

        return moveAngle;
    }

    private float randomLerp(float min, float max) {
        return MathHelper.lerp(new SecureRandom().nextFloat(), min, max);
    }

    @Override
    public Vec3d randomValue() {
        return new Vec3d(0, 0, 0);
    }
}
