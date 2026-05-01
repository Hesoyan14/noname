package com.example.client.feature.event.impl;

import com.example.client.feature.event.Event;
import com.example.client.utils.render.RenderContext;

public class Render2DEvent extends Event {
    private final RenderContext context;

    public Render2DEvent(RenderContext context) {
        super(true);
        this.context = context;
    }

    public RenderContext getContext() { return context; }
}
