package itz.silentcore.feature.event.impl;

import itz.silentcore.feature.event.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.Vec3d;

@Getter
@Setter
public class MoveEvent extends Event {
    private Vec3d movement;

    public MoveEvent(Vec3d movement, boolean pre) {
        super(pre);
        this.movement = movement;
    }
}
