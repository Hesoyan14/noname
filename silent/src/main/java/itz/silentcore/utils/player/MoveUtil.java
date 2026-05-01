package itz.silentcore.utils.player;

import net.minecraft.util.math.Vec3d;

import static itz.silentcore.utils.client.IMinecraft.mc;

public class MoveUtil {
    public static float speedSqrt() {
        if (mc == null || mc.player == null) {
            return 0.0F;
        }

        try {
            Vec3d velocity = mc.player.getVelocity();
            return (float) Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        } catch (Exception e) {
            return 0.0F;
        }
    }
}
