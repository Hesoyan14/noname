package itz.silentcore.feature.event;

import itz.silentcore.SilentCore;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Event {
    private boolean cancelled = false;
    @Setter
    private boolean pre;

    public Event(boolean pre) {
        this.pre = pre;
    }

    public void hook() {
        SilentCore.getInstance().eventBus.post(this);
    }

    public void cancel() {
        cancelled = true;
    }
}