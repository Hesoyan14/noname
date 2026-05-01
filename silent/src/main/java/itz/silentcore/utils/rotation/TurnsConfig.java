package itz.silentcore.utils.rotation;

import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

@Getter
public class TurnsConfig {
    public static TurnsConfig DEFAULT = new TurnsConfig(new LinearConstructor(), true, true);
    
    private final RotateConstructor angleSmooth;
    private final boolean moveCorrection;
    private final boolean freeCorrection;
    private final int resetThreshold = 1;

    public TurnsConfig(RotateConstructor angleSmooth, boolean moveCorrection, boolean freeCorrection) {
        this.angleSmooth = angleSmooth;
        this.moveCorrection = moveCorrection;
        this.freeCorrection = freeCorrection;
    }

    public TurnsConstructor createRotationPlan(Turns angle, Vec3d vec, Entity entity, int reset) {
        return new TurnsConstructor(angle, vec, entity, angleSmooth, reset, resetThreshold, moveCorrection, freeCorrection);
    }
}
