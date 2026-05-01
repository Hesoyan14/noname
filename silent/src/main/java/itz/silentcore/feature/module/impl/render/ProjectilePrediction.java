package itz.silentcore.feature.module.impl.render;

import com.google.common.eventbus.Subscribe;
import itz.silentcore.feature.event.impl.WorldRenderEvent;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.feature.theme.ThemeManager;
import itz.silentcore.utils.client.IMinecraft;
import itz.silentcore.utils.render.Render3D;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.List;

@ModuleAnnotation(name = "ProjectilePrediction", category = Category.RENDER, description = "Shows projectile trajectory")
public class ProjectilePrediction extends Module implements IMinecraft {
    
    @Subscribe
    public void onWorldRender(WorldRenderEvent event) {
        if (mc.player == null || mc.world == null) return;
        
        getProjectiles().forEach(entity -> {
            Vec3d motion = entity.getVelocity();
            Vec3d pos = entity.getPos();
            Vec3d prevPos;
            
            int color = ThemeManager.getInstance().getPrimaryColor();
            
            for (int i = 0; i < 300; i++) {
                prevPos = pos;
                pos = pos.add(motion);
                motion = calculateMotion(entity, prevPos, motion);
                
                HitResult result = mc.world.raycast(new RaycastContext(
                    prevPos,
                    pos,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    entity
                ));
                
                if (!result.getType().equals(HitResult.Type.MISS)) {
                    pos = result.getPos();
                }
                
                float alpha = Math.min(1.0f, i / 25.0f);
                Render3D.drawLine(prevPos, pos, color, 2, false);
                
                if (!result.getType().equals(HitResult.Type.MISS) || pos.y < -128) {
                    break;
                }
            }
        });
    }
    
    private List<Entity> getProjectiles() {
        List<Entity> projectiles = new ArrayList<>();
        
        if (mc.world == null) return projectiles;
        
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PersistentProjectileEntity || 
                entity instanceof ThrownItemEntity || 
                entity instanceof ItemEntity) {
                if (!isVisible(entity)) {
                    projectiles.add(entity);
                }
            }
        }
        
        return projectiles;
    }
    
    private Vec3d calculateMotion(Entity entity, Vec3d prevPos, Vec3d motion) {
        boolean isInWater = mc.world.getFluidState(BlockPos.ofFloored(prevPos)).isIn(FluidTags.WATER);
        
        double multiply = isInWater ? 0.8 : 0.99;
        
        if (entity instanceof PersistentProjectileEntity && isInWater) {
            multiply = 0.6;
        }
        
        return motion.multiply(multiply).add(0, -entity.getFinalGravity(), 0);
    }
    
    private boolean isVisible(Entity entity) {
        boolean posChange = entity.getX() == entity.prevX && 
                           entity.getY() == entity.prevY && 
                           entity.getZ() == entity.prevZ;
        return posChange || entity.isOnGround();
    }
}
