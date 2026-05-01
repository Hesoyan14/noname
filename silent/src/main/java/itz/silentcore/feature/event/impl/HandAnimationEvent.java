package itz.silentcore.feature.event.impl;

import itz.silentcore.feature.event.Event;
import lombok.Getter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;

@Getter
public class HandAnimationEvent extends Event {
    private final MatrixStack matrices;
    private final Hand hand;
    private final float swingProgress;

    public HandAnimationEvent(MatrixStack matrices, Hand hand, float swingProgress) {
        super(true);
        this.matrices = matrices;
        this.hand = hand;
        this.swingProgress = swingProgress;
    }
}
