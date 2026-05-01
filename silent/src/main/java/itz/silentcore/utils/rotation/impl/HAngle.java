package itz.silentcore.utils.rotation.impl;

import itz.silentcore.utils.math.Calculate;
import itz.silentcore.utils.rotation.MathAngle;
import itz.silentcore.utils.rotation.RotateConstructor;
import itz.silentcore.utils.rotation.Turns;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.security.SecureRandom;

public class HAngle extends RotateConstructor {
    private final SecureRandom secureRandom = new SecureRandom();

    public HAngle() {
        super("HvH");
    }

    @Override
    public Turns limitAngleChange(Turns currentAngle, Turns targetAngle, Vec3d vec3d, Entity entity) {
        Turns angleDelta = MathAngle.calculateDelta(currentAngle, targetAngle);
        float yawDelta = angleDelta.getYaw();
        float pitchDelta = angleDelta.getPitch();
        float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));

        float speed = 1;
        float jitterYaw = (float) (5 * Math.sin(System.currentTimeMillis() / 45D));
        float jitterPitch = (float) (5 * Math.sin(System.currentTimeMillis() / 45D));
        
        float maxRotation = 360F;
        float lineYaw = (Math.abs(yawDelta / rotationDifference) * maxRotation);
        float linePitch = (Math.abs(pitchDelta / rotationDifference) * maxRotation);

        float moveYaw = MathHelper.clamp(yawDelta, -lineYaw, lineYaw);
        float movePitch = MathHelper.clamp(pitchDelta, -linePitch, linePitch);

        Turns moveAngle = new Turns(currentAngle.getYaw(), currentAngle.getPitch());
        moveAngle.setYaw(MathHelper.lerp(Calculate.getRandom(speed, speed + 0.2F), currentAngle.getYaw(), currentAngle.getYaw() + moveYaw) + jitterYaw);
        moveAngle.setPitch(MathHelper.lerp(Calculate.getRandom(speed, speed + 0.2F), currentAngle.getPitch(), currentAngle.getPitch() + movePitch) + jitterPitch);

        return new Turns(moveAngle.getYaw(), moveAngle.getPitch());
    }

    @Override
    public Vec3d randomValue() {
        return new Vec3d(0, 0, 0);
    }
}
