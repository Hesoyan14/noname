package itz.silentcore.feature.event.impl;

import itz.silentcore.feature.event.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FovEvent extends Event {
    private float fov;

    public FovEvent(float fov, boolean pre) {
        super(pre);
        this.fov = fov;
    }
}
