package itz.silentcore.utils.rotation;

import itz.silentcore.utils.math.Calculate;
import lombok.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class Turns {
    public static Turns DEFAULT = new Turns(0, 0);
    private float yaw, pitch;

    public static Turns fromVec3d(Vec3d vector) {
        float yaw = (float) Math.toDegrees(Math.atan2(vector.z, vector.x)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(vector.y, Math.hypot(vector.x, vector.z)));
        return new Turns(MathHelper.wrapDegrees(yaw), MathHelper.clamp(pitch, -90.0f, 90.0f));
    }

    public Turns adjustSensitivity(Turns previousAngle) {
        double gcd = Calculate.computeGcd();
        float adjustedYaw = adjustAxis(yaw, previousAngle.yaw, gcd);
        float adjustedPitch = adjustAxis(pitch, previousAngle.pitch, gcd);
        return new Turns(adjustedYaw, MathHelper.clamp(adjustedPitch, -90f, 90f));
    }

    public Turns random(float f) {
        return new Turns(yaw + Calculate.getRandom(-f, f), pitch + Calculate.getRandom(-f, f));
    }

    private float adjustAxis(float axisValue, float previousValue, double gcd) {
        float delta = axisValue - previousValue;
        return previousValue + Math.round(delta / gcd) * (float) gcd;
    }

    public final Vec3d toVector() {
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }

    public Turns copy() {
        return new Turns(yaw, pitch);
    }

    @ToString
    @Getter
    @RequiredArgsConstructor
    public static class VecRotation {
        private final Turns angle;
        private final Vec3d vec;
    }
}
