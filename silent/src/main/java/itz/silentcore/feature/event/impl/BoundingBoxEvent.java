package itz.silentcore.feature.event.impl;

import itz.silentcore.feature.event.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

@Getter
@Setter
public class BoundingBoxEvent extends Event {
    private Box box;
    private final Entity entity;
    
    public BoundingBoxEvent(Box box, Entity entity) {
        super(false);
        this.box = box;
        this.entity = entity;
    }
}
