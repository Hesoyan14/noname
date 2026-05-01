package itz.silentcore.utils.rotation.impl;

import itz.silentcore.utils.math.StopWatch;
import itz.silentcore.utils.rotation.MathAngle;
import itz.silentcore.utils.rotation.RotateConstructor;
import itz.silentcore.utils.rotation.Turns;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.security.SecureRandom;

public class FTAngle extends RotateConstructor {
    private final StopWatch attackTimer = new StopWatch();
    private int count = 0;

    public FTAngle() {
        super("FunTime");
    }

    @Override
    public Turns limitAngleChange(Turns currentTurns, Turns targetTurns, Vec3d vec3d, Entity entity) {
        Turns turnsDelta = MathAngle.calculateDelta(currentTurns, targetTurns);
        float yawDelta = turnsDelta.getYaw();
        float pitchDelta = turnsDelta.getPitch();
        float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));

        if (entity != null) {
            float speed = 1.0f;
            float lineYaw = (Math.abs(yawDelta / rotationDifference) * 180);
            float linePitch = (Math.abs(pitchDelta / rotationDifference) * 180);

            float moveYaw = MathHelper.clamp(yawDelta, -lineYaw, lineYaw);
            float movePitch = MathHelper.clamp(pitchDelta, -linePitch, linePitch);

            Turns moveTurns = new Turns(currentTurns.getYaw(), currentTurns.getPitch());
            moveTurns.setYaw(MathHelper.lerp(randomLerp(speed, speed + 0.2F), currentTurns.getYaw(), currentTurns.getYaw() + moveYaw));
            moveTurns.setPitch(MathHelper.lerp(randomLerp(speed, speed + 0.2F), currentTurns.getPitch(), currentTurns.getPitch() + movePitch));

            return moveTurns;
        } else {
            int suck = count % 3;
            float speed = attackTimer.finished(430) ? new SecureRandom().nextBoolean() ? 0.4F : 0.2F : -0.2F;
            float random = attackTimer.getElapsedTime() / 40F + (count % 6);

            Turns randomTurns = switch (suck) {
                case 0 -> new Turns((float) Math.cos(random), (float) Math.sin(random));
                case 1 -> new Turns((float) Math.sin(random), (float) Math.cos(random));
                case 2 -> new Turns((float) Math.sin(random), (float) -Math.cos(random));
                default -> new Turns((float) -Math.cos(random), (float) Math.sin(random));
            };

            float yaw = !attackTimer.finished(2000) ? randomLerp(12, 24) * randomTurns.getYaw() : 0;
            float pitch2 = randomLerp(0, 2) * (float) Math.cos((double) System.currentTimeMillis() / 5000);
            float pitch = !attackTimer.finished(2000) ? randomLerp(2, 6) * randomTurns.getPitch() + pitch2 : 0;

            float lineYaw = (Math.abs(yawDelta / rotationDifference) * 180);
            float linePitch = (Math.abs(pitchDelta / rotationDifference) * 180);

            float moveYaw = MathHelper.clamp(yawDelta, -lineYaw, lineYaw);
            float movePitch = MathHelper.clamp(pitchDelta, -linePitch, linePitch);

            Turns moveTurns = new Turns(currentTurns.getYaw(), currentTurns.getPitch());
            moveTurns.setYaw(MathHelper.lerp(Math.clamp(randomLerp(speed, speed + 0.2F), 0, 1), currentTurns.getYaw(), currentTurns.getYaw() + moveYaw) + yaw);
            moveTurns.setPitch(MathHelper.lerp(Math.clamp(randomLerp(speed, speed + 0.2F), 0, 1), currentTurns.getPitch(), currentTurns.getPitch() + movePitch) + pitch);

            return moveTurns;
        }
    }

    @Override
    public Vec3d randomValue() {
        return new Vec3d(0, 0, 0);
    }

    private float randomLerp(float min, float max) {
        return MathHelper.lerp(new SecureRandom().nextFloat(), min, max);
    }
}
