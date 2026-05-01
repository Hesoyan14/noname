package itz.silentcore.utils.rotation.impl;

import itz.silentcore.utils.rotation.MathAngle;
import itz.silentcore.utils.rotation.RotateConstructor;
import itz.silentcore.utils.rotation.Turns;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.security.SecureRandom;

public class MatrixAngle extends RotateConstructor {
    public MatrixAngle() {
        super("Matrix");
    }

    @Override
    public Turns limitAngleChange(Turns currentAngle, Turns targetAngle, Vec3d vec3d, Entity entity) {
        Turns angleDelta = MathAngle.calculateDelta(currentAngle, targetAngle);
        float yawDelta = angleDelta.getYaw();
        float pitchDelta = angleDelta.getPitch();
        float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));

        float speed = 1.0f;
        float lineYaw = (Math.abs(yawDelta / rotationDifference) * 360);
        float linePitch = (Math.abs(pitchDelta / rotationDifference) * 180);
        
        float jitterYaw = (float) (randomLerp(0, 6) * Math.sin(System.currentTimeMillis() / randomLerp(15F, 145F)));
        float jitterPitch = (float) (randomLerp(1, 3) * Math.sin(System.currentTimeMillis() / randomLerp(15F, 145F)));

        float moveYaw = MathHelper.clamp(yawDelta, -lineYaw, lineYaw);
        float movePitch = MathHelper.clamp(pitchDelta, -linePitch, linePitch);
        
        Turns moveAngle = new Turns(currentAngle.getYaw(), currentAngle.getPitch());
        moveAngle.setYaw(MathHelper.lerp(speed, currentAngle.getYaw(), currentAngle.getYaw() + moveYaw) + jitterYaw);
        moveAngle.setPitch(MathHelper.lerp(speed, currentAngle.getPitch(), currentAngle.getPitch() + movePitch) + jitterPitch);

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
