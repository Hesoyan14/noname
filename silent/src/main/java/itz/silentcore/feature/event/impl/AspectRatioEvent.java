package itz.silentcore.feature.event.impl;

import itz.silentcore.feature.event.Event;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AspectRatioEvent extends Event {
    private float ratio;

    public AspectRatioEvent() {
        super(true);
    }
}
