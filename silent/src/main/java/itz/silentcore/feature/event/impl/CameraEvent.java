package itz.silentcore.feature.event.impl;

import itz.silentcore.feature.event.Event;
import itz.silentcore.utils.rotation.Turns;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CameraEvent extends Event {
    private Turns angle;

    public CameraEvent(Turns angle, boolean pre) {
        super(pre);
        this.angle = angle;
    }
}
