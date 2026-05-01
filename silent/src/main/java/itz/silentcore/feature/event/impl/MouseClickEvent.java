package itz.silentcore.feature.event.impl;

import itz.silentcore.feature.event.Event;
import lombok.Getter;

@Getter
public class MouseClickEvent extends Event {
    private double mouseX;
    private double mouseY;
    private int button;

    public MouseClickEvent(double mouseX, double mouseY, int button) {
        super(false);
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.button = button;
    }
}
