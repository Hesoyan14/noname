package itz.silentcore.feature.event.impl;

import itz.silentcore.feature.event.Event;
import lombok.Getter;

@Getter
public class KeyEvent extends Event {
    private final int key;
    private final int action;

    public KeyEvent(int key, int action, boolean pre) {
        super(pre);
        this.key = key;
        this.action = action;
    }

    public boolean isKeyDown(int targetKey) {
        return key == targetKey && action == 1; // GLFW_PRESS
    }
}
