package itz.silentcore.utils.math;

import itz.silentcore.utils.client.IMinecraft;
import lombok.experimental.UtilityClass;

import java.util.Random;

@UtilityClass
public class Calculate implements IMinecraft {
    private static final Random RANDOM = new Random();

    public static float getRandom(float min, float max) {
        return min + RANDOM.nextFloat() * (max - min);
    }

    public static double computeGcd() {
        float sensitivity = mc.options.getMouseSensitivity().getValue().floatValue();
        return (sensitivity * 0.6F + 0.2F) * (sensitivity * 0.6F + 0.2F) * (sensitivity * 0.6F + 0.2F) * 1.2F;
    }

    public static net.minecraft.util.math.Vec3d interpolate(net.minecraft.util.math.Vec3d start, net.minecraft.util.math.Vec3d end) {
        if (mc.getRenderTickCounter() == null) return end;
        float tickDelta = mc.getRenderTickCounter().getTickDelta(false);
        return new net.minecraft.util.math.Vec3d(
            start.x + (end.x - start.x) * tickDelta,
            start.y + (end.y - start.y) * tickDelta,
            start.z + (end.z - start.z) * tickDelta
        );
    }

    public static net.minecraft.util.math.Vec3d interpolate(net.minecraft.entity.Entity entity) {
        if (entity == null || mc.getRenderTickCounter() == null) return net.minecraft.util.math.Vec3d.ZERO;
        float tickDelta = mc.getRenderTickCounter().getTickDelta(false);
        return new net.minecraft.util.math.Vec3d(
            entity.prevX + (entity.getX() - entity.prevX) * tickDelta,
            entity.prevY + (entity.getY() - entity.prevY) * tickDelta,
            entity.prevZ + (entity.getZ() - entity.prevZ) * tickDelta
        );
    }
}
