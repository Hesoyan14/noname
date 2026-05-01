package itz.silentcore.feature.event.impl;

import itz.silentcore.feature.event.Event;
import itz.silentcore.utils.render.RenderContext;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Render2DEvent extends Event {
    private RenderContext context;

    public Render2DEvent(RenderContext context) {
        super(false);
        this.context = context;
    }
}