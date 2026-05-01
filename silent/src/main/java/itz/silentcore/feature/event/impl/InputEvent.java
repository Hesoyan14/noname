package itz.silentcore.feature.event.impl;

import itz.silentcore.feature.event.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.input.Input;

@Getter
@Setter
public class InputEvent extends Event {
    private Input input;
    private float forward;
    private float sideways;

    public InputEvent(Input input, float forward, float sideways, boolean pre) {
        super(pre);
        this.input = input;
        this.forward = forward;
        this.sideways = sideways;
    }

    public boolean forward() {
        return forward > 0;
    }

    public boolean sideways() {
        return sideways != 0;
    }

    public void inputNone() {
        input.movementForward = 0;
        input.movementSideways = 0;
        // Jump and sneak are handled through PlayerInput, not Input fields
    }
}
