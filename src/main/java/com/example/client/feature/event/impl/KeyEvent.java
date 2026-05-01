package com.example.client.feature.event.impl;

import com.example.client.feature.event.Event;

public class KeyEvent extends Event {
    private final int key;
    private final int action;

    public KeyEvent(int key, int action) {
        super(true);
        this.key = key;
        this.action = action;
    }

    public int getKey() { return key; }
    public int getAction() { return action; }
}
