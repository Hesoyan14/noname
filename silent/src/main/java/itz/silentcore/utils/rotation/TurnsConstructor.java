package itz.silentcore.utils.rotation;

import itz.silentcore.utils.client.IMinecraft;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

@Setter
@Getter
@RequiredArgsConstructor
public class TurnsConstructor implements IMinecraft {
    private final Turns angle;
    private final Vec3d vec3d;
    private final Entity entity;
    private final RotateConstructor angleSmooth;
    private final int ticksUntilReset;
    private final float resetThreshold;
    private final boolean moveCorrection;
    private final boolean freeCorrection;

    public Turns nextRotation(Turns fromAngle, boolean isResetting) {
        if (isResetting) {
            return angleSmooth.limitAngleChange(fromAngle, MathAngle.cameraAngle());
        }
        return angleSmooth.limitAngleChange(fromAngle, angle, vec3d, entity);
    }
}
