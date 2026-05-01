package itz.silentcore.utils.combat;

import itz.silentcore.utils.client.IMinecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import itz.silentcore.utils.rotation.Turns;

public class MultiPoint implements IMinecraft {

    public Vec3d computeVector(LivingEntity target, float range, Turns rotation, float randomValue, boolean ignoreWalls) {
        if (target == null) return Vec3d.ZERO;
        
        Box box = target.getBoundingBox();
        Vec3d center = box.getCenter();
        
        // Добавляем небольшую рандомизацию для более естественной атаки
        double offsetX = (Math.random() - 0.5) * randomValue * 0.1;
        double offsetY = (Math.random() - 0.5) * randomValue * 0.1;
        double offsetZ = (Math.random() - 0.5) * randomValue * 0.1;
        
        return center.add(offsetX, offsetY, offsetZ);
    }

    public boolean hasValidPoint(LivingEntity target, float range, boolean ignoreWalls) {
        if (target == null || mc.player == null) return false;
        
        double distance = mc.player.distanceTo(target);
        if (distance > range) return false;
        
        if (!ignoreWalls && !mc.player.canSee(target)) {
            return false;
        }
        
        return true;
    }
}
