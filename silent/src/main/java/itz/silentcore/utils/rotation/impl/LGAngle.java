package itz.silentcore.utils.rotation.impl;

import itz.silentcore.utils.rotation.MathAngle;
import itz.silentcore.utils.rotation.RotateConstructor;
import itz.silentcore.utils.rotation.Turns;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.security.SecureRandom;

public class LGAngle extends RotateConstructor {
    public LGAngle() {
        super("CakeWorld");
    }

    @Override
    public Turns limitAngleChange(Turns currentAngle, Turns targetAngle, Vec3d vec3d, Entity entity) {
        Turns angleDelta = MathAngle.calculateDelta(currentAngle, targetAngle);
        float yawDelta = angleDelta.getYaw();
        float pitchDelta = angleDelta.getPitch();
        float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));

        float baseSpeed = 0.93F;
        float speed = baseSpeed;

        float lineYaw = (Math.abs(yawDelta / rotationDifference) * 180);
        float linePitch = (Math.abs(pitchDelta / rotationDifference) * 180);
        
        float jitterYaw = (float) (randomLerp(20, 26) * Math.sin(System.currentTimeMillis() / 25D));
        float jitterPitch = (float) (randomLerp(8, 23) * Math.sin(System.currentTimeMillis() / 27D));

        float moveYaw = MathHelper.clamp(yawDelta, -lineYaw, lineYaw);
        float movePitch = MathHelper.clamp(pitchDelta, -linePitch, linePitch);
        
        Turns moveAngle = new Turns(currentAngle.getYaw(), currentAngle.getPitch());
        moveAngle.setYaw(MathHelper.lerp(baseSpeed, currentAngle.getYaw(), currentAngle.getYaw() + moveYaw) + jitterYaw);
        moveAngle.setPitch(MathHelper.lerp(baseSpeed, currentAngle.getPitch(), currentAngle.getPitch() + movePitch) + jitterPitch);

        return moveAngle;
    }

    private float randomLerp(float min, float max) {
        return MathHelper.lerp(new SecureRandom().nextFloat(), min, max);
    }

    @Override
    public Vec3d randomValue() {
        return new Vec3d(0.01, 0.07, 0.02);
    }
}
