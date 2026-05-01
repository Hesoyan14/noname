package itz.silentcore.utils.rotation;

import itz.silentcore.utils.client.IMinecraft;
import lombok.experimental.UtilityClass;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@UtilityClass
public class MathAngle implements IMinecraft {
    
    public static Turns fromVec3d(Vec3d vector) {
        return Turns.fromVec3d(vector);
    }

    public static Turns calculateAngle(Vec3d to) {
        return fromVec3d(to.subtract(mc.player.getEyePos()));
    }

    public static Turns cameraAngle() {
        return new Turns(mc.player.getYaw(), mc.player.getPitch());
    }

    public static float computeAngleDifference(float a, float b) {
        return MathHelper.wrapDegrees(a - b);
    }

    public static double computeRotationDifference(Turns a, Turns b) {
        return Math.hypot(
            Math.abs(computeAngleDifference(a.getYaw(), b.getYaw())), 
            Math.abs(a.getPitch() - b.getPitch())
        );
    }

    public static Turns calculateDelta(Turns start, Turns end) {
        float deltaYaw = MathHelper.wrapDegrees(end.getYaw() - start.getYaw());
        float deltaPitch = MathHelper.wrapDegrees(end.getPitch() - start.getPitch());
        return new Turns(deltaYaw, deltaPitch);
    }
}
