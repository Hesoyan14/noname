package itz.silentcore.feature.event.impl;

import itz.silentcore.feature.event.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.Vec3d;

@Getter
@Setter
public class CameraPositionEvent extends Event {
    private Vec3d pos;

    public CameraPositionEvent(Vec3d pos, boolean pre) {
        super(pre);
        this.pos = pos;
    }
}
