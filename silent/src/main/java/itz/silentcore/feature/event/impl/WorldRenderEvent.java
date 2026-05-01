package itz.silentcore.feature.event.impl;

import itz.silentcore.feature.event.Event;
import lombok.Getter;
import net.minecraft.client.util.math.MatrixStack;

@Getter
public class WorldRenderEvent extends Event {
    private final MatrixStack matrices;
    private final float tickDelta;

    public WorldRenderEvent(MatrixStack matrices, float tickDelta, boolean pre) {
        super(pre);
        this.matrices = matrices;
        this.tickDelta = tickDelta;
    }
}
