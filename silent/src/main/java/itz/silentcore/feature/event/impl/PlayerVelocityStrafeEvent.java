package itz.silentcore.feature.event.impl;

import itz.silentcore.feature.event.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.Vec3d;

@Getter
@Setter
public class PlayerVelocityStrafeEvent extends Event {
    private final Vec3d movementInput;
    private final float speed;
    private final float yaw;
    private Vec3d velocity;

    public PlayerVelocityStrafeEvent(Vec3d movementInput, float speed, float yaw, Vec3d velocity) {
        super(false);
        this.movementInput = movementInput;
        this.speed = speed;
        this.yaw = yaw;
        this.velocity = velocity;
    }
}
