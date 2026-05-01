package itz.silentcore.utils.rotation.impl;

import itz.silentcore.utils.rotation.MathAngle;
import itz.silentcore.utils.rotation.RotateConstructor;
import itz.silentcore.utils.rotation.Turns;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SnapAngle extends RotateConstructor {
    public SnapAngle() {
        super("Snap");
    }

    @Override
    public Turns limitAngleChange(Turns currentAngle, Turns targetAngle, Vec3d vec3d, Entity entity) {
        Turns angleDelta = MathAngle.calculateDelta(currentAngle, targetAngle);
        float yawDelta = angleDelta.getYaw();
        float pitchDelta = angleDelta.getPitch();
        float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));

        float speed = 1f;
        float maxRotation = 180.0f;
        
        float lineYaw = (Math.abs(yawDelta / rotationDifference) * maxRotation);
        float linePitch = (Math.abs(pitchDelta / rotationDifference) * maxRotation);

        float moveYaw = MathHelper.clamp(yawDelta, -lineYaw, lineYaw);
        float movePitch = MathHelper.clamp(pitchDelta, -linePitch, linePitch);

        Turns moveAngle = new Turns(currentAngle.getYaw(), currentAngle.getPitch());
        moveAngle.setYaw(MathHelper.lerp(speed, currentAngle.getYaw(), currentAngle.getYaw() + moveYaw));
        moveAngle.setPitch(MathHelper.lerp(speed, currentAngle.getPitch(), currentAngle.getPitch() + movePitch));

        return new Turns(moveAngle.getYaw(), moveAngle.getPitch());
    }

    @Override
    public Vec3d randomValue() {
        return new Vec3d(0, 0, 0);
    }
}
