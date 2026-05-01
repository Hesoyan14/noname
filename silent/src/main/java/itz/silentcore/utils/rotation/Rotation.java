package itz.silentcore.utils.rotation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Getter
@Setter
@AllArgsConstructor
public class Rotation {
    private float yaw;
    private float pitch;

    public static Rotation fromVec3d(Vec3d vec) {
        double x = vec.x;
        double y = vec.y;
        double z = vec.z;
        
        double horizontalDistance = Math.sqrt(x * x + z * z);
        
        float yaw = (float) Math.toDegrees(Math.atan2(z, x)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(y, horizontalDistance));
        
        return new Rotation(MathHelper.wrapDegrees(yaw), MathHelper.clamp(pitch, -90.0f, 90.0f));
    }

    public Vec3d toVec3d() {
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }

    public Rotation copy() {
        return new Rotation(yaw, pitch);
    }
}
