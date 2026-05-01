package itz.silentcore.feature.event.impl;

import itz.silentcore.feature.event.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FogEvent extends Event {
    private float distance;
    private int color;

    public FogEvent() {
        super(false);
    }
}
