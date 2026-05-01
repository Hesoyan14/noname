package itz.silentcore.feature.event.impl;

import itz.silentcore.feature.event.Event;
import lombok.Getter;
import net.minecraft.entity.player.PlayerEntity;

@Getter
public class JumpEvent extends Event {
    private final PlayerEntity player;

    public JumpEvent(PlayerEntity player) {
        super(false);
        this.player = player;
    }
}
