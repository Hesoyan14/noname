package itz.silentcore.feature.event.impl;

import itz.silentcore.feature.event.Event;
import itz.silentcore.utils.render.RenderContext;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TabRenderEvent extends Event {
    private RenderContext context;
    private int scaledWindowWidth;

    public TabRenderEvent(RenderContext context, int scaledWindowWidth) {
        super(false);
        this.context = context;
        this.scaledWindowWidth = scaledWindowWidth;
    }
}
