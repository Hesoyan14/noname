package itz.silentcore.utils.rotation.impl;

import itz.silentcore.utils.rotation.MathAngle;
import itz.silentcore.utils.rotation.RotateConstructor;
import itz.silentcore.utils.rotation.Turns;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.security.SecureRandom;

public class SPAngle extends RotateConstructor {
    private static final float ROTATION_SPEED = 25.5F;
    private static final float LIMIT_ROTATION_SPEED = 44.5F;
    private static final float SHAKE_INTENSITY = 2.2F;
    private static final float SHAKE_SPEED = 0.32F;
    private static final float EPSILON = 1.0E-3F;
    private static final SecureRandom RANDOM = new SecureRandom();

    public SPAngle() {
        super("SpookyTime");
    }

    @Override
    public Turns limitAngleChange(Turns currentAngle, Turns targetAngle, Vec3d vec3d, Entity entity) {
        Turns delta = MathAngle.calculateDelta(currentAngle, targetAngle);
        float yawDelta = delta.getYaw();
        float pitchDelta = delta.getPitch();
        float length = (float) Math.hypot(yawDelta, pitchDelta);
        
        final float ANGLE_LIMIT_YAW = (float) Math.min(Math.abs(yawDelta), 74 + (Math.random() * 1.0329834f));
        final float ANGLE_LIMIT_PITCH = (float) Math.min(Math.abs(pitchDelta), 32.334);
        
        Turns moveAngle = new Turns(currentAngle.getYaw(), currentAngle.getPitch());

        if (length > EPSILON) {
            boolean limitReached = Math.abs(pitchDelta) >= ANGLE_LIMIT_PITCH;
            float maxStep = limitReached ? LIMIT_ROTATION_SPEED : ROTATION_SPEED;
            float step = Math.min(length, maxStep);
            float scale = step / length;
            
            if (!limitReached) {
                scale = easeTowardsTarget(scale);
            }

            float newPitch = MathHelper.clamp(currentAngle.getPitch() + pitchDelta * scale, -89.0F, 90.0F);
            moveAngle.setPitch(newPitch);
        }

        if (length > EPSILON) {
            boolean limitReached = Math.abs(yawDelta) >= ANGLE_LIMIT_YAW;
            float maxStep = limitReached ? LIMIT_ROTATION_SPEED : ROTATION_SPEED;
            float step = Math.min(length, maxStep);
            float scale = step / length;
            
            if (!limitReached) {
                scale = easeTowardsTarget(scale);
            }

            float newYaw = currentAngle.getYaw() + yawDelta * scale;
            moveAngle.setYaw(newYaw);
        }

        return moveAngle;
    }

    private float easeTowardsTarget(float value) {
        return value * (0.5F + 0.5F * value);
    }

    @Override
    public Vec3d randomValue() {
        return new Vec3d(0.1, 0.1, 0.1);
    }
}
