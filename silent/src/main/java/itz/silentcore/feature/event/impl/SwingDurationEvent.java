package itz.silentcore.feature.event.impl;

import itz.silentcore.feature.event.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SwingDurationEvent extends Event {
    private float animation;

    public SwingDurationEvent() {
        super(true);
    }
}
