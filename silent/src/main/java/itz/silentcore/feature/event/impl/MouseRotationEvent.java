package itz.silentcore.feature.event.impl;

import itz.silentcore.feature.event.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MouseRotationEvent extends Event {
    private double cursorDeltaX;
    private double cursorDeltaY;

    public MouseRotationEvent(double cursorDeltaX, double cursorDeltaY, boolean pre) {
        super(pre);
        this.cursorDeltaX = cursorDeltaX;
        this.cursorDeltaY = cursorDeltaY;
    }
}
