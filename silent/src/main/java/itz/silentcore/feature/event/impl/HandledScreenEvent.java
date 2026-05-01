package itz.silentcore.feature.event.impl;

import itz.silentcore.feature.event.Event;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.screen.slot.Slot;

@Getter
public class HandledScreenEvent extends Event {
    private final DrawContext drawContext;
    private final Slot slotHover;
    private final int backgroundWidth;
    private final int backgroundHeight;

    public HandledScreenEvent(DrawContext drawContext, Slot slotHover, int backgroundWidth, int backgroundHeight) {
        super(false);
        this.drawContext = drawContext;
        this.slotHover = slotHover;
        this.backgroundWidth = backgroundWidth;
        this.backgroundHeight = backgroundHeight;
    }
}
