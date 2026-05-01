package itz.silentcore.utils.rotation;

import itz.silentcore.utils.client.IMinecraft;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

@Getter
@RequiredArgsConstructor
public abstract class RotateConstructor implements IMinecraft {
    private final String name;

    public Turns limitAngleChange(Turns currentAngle, Turns targetAngle) {
        return limitAngleChange(currentAngle, targetAngle, null, null);
    }

    public Turns limitAngleChange(Turns currentAngle, Turns targetAngle, Vec3d vec3d) {
        return limitAngleChange(currentAngle, targetAngle, vec3d, null);
    }

    public abstract Turns limitAngleChange(Turns currentAngle, Turns targetAngle, Vec3d vec3d, Entity entity);

    public abstract Vec3d randomValue();
}
