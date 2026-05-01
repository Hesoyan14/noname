package com.example.client.feature.event;

import com.example.client.NoNameClient;

public class Event {
    private boolean cancelled = false;
    private final boolean pre;

    public Event(boolean pre) {
        this.pre = pre;
    }

    public boolean isPre() { return pre; }
    public boolean isCancelled() { return cancelled; }
    public void cancel() { cancelled = true; }

    public void hook() {
        NoNameClient.getInstance().getEventBus().post(this);
    }
}
