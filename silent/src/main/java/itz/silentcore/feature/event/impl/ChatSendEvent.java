package itz.silentcore.feature.event.impl;

import itz.silentcore.feature.event.Event;
import lombok.Getter;

@Getter
public class ChatSendEvent extends Event {
    private final String message;

    public ChatSendEvent(String message) {
        super(true);
        this.message = message;
    }
}
