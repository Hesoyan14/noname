package itz.silentcore.feature.event.impl;

import itz.silentcore.feature.event.Event;
import lombok.Getter;
import net.minecraft.network.packet.Packet;

@Getter
public class PacketEvent extends Event {
    private final Packet<?> packet;
    private final boolean send;

    public PacketEvent(Packet<?> packet, boolean send) {
        super(false);
        this.packet = packet;
        this.send = send;
    }

    public boolean isReceive() {
        return !send;
    }
}
