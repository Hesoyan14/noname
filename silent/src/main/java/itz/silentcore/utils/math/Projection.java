package itz.silentcore.utils.math;

import com.mojang.blaze3d.systems.RenderSystem;
import itz.silentcore.utils.client.IMinecraft;
import itz.silentcore.utils.render.Render3D;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4d;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

public class Projection implements IMinecraft {
    
    public static Matrix4f lastProjMat = new Matrix4f();
    
    public static Vec3d worldToScreen(Vec3d pos) {
        Vector3f delta = pos.toVector3f();
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        Vector3f target = new Vector3f();

        Vector4f transformedCoordinates = new Vector4f(delta.x, delta.y, delta.z, 1.f)
            .mul(Render3D.lastWorldSpaceMatrix.getPositionMatrix());
        Matrix4f matrixProj = new Matrix4f(lastProjMat);
        matrixProj.project(transformedCoordinates.x(), transformedCoordinates.y(), 
            transformedCoordinates.z(), viewport, target);

        return new Vec3d(
            target.x / mc.getWindow().getScaleFactor(), 
            (mc.getWindow().getHeight() - target.y) / mc.getWindow().getScaleFactor(), 
            target.z
        );
    }
    
    public static Vector4d getEntityBox(Entity entity, Vec3d interpolatedPos) {
        Vector4d position = null;
        
        for (Vec3d vector : getEntityCorners(entity, interpolatedPos)) {
            Vec3d screen = worldToScreen(vector);
            if (screen.z > 0 && screen.z < 1) {
                if (position == null) {
                    position = new Vector4d(screen.x, screen.y, screen.z, 0);
                }
                position.x = Math.min(screen.x, position.x);
                position.y = Math.min(screen.y, position.y);
                position.z = Math.max(screen.x, position.z);
                position.w = Math.max(screen.y, position.w);
            }
        }
        
        return position;
    }
    
    private static Vec3d[] getEntityCorners(Entity entity, Vec3d pos) {
        Box box = entity.getBoundingBox();
        Box adjustedBox = new Box(
            box.minX - entity.getX() + pos.x - 0.1,
            box.minY - entity.getY() + pos.y - 0.1,
            box.minZ - entity.getZ() + pos.z - 0.1,
            box.maxX - entity.getX() + pos.x + 0.1,
            box.maxY - entity.getY() + pos.y + 0.1,
            box.maxZ - entity.getZ() + pos.z + 0.1
        );
        
        return new Vec3d[]{
            new Vec3d(adjustedBox.minX, adjustedBox.minY, adjustedBox.minZ),
            new Vec3d(adjustedBox.minX, adjustedBox.maxY, adjustedBox.minZ),
            new Vec3d(adjustedBox.maxX, adjustedBox.minY, adjustedBox.minZ),
            new Vec3d(adjustedBox.maxX, adjustedBox.maxY, adjustedBox.minZ),
            new Vec3d(adjustedBox.minX, adjustedBox.minY, adjustedBox.maxZ),
            new Vec3d(adjustedBox.minX, adjustedBox.maxY, adjustedBox.maxZ),
            new Vec3d(adjustedBox.maxX, adjustedBox.minY, adjustedBox.maxZ),
            new Vec3d(adjustedBox.maxX, adjustedBox.maxY, adjustedBox.maxZ)
        };
    }
    
    public static boolean isOnScreen(Vector4d box) {
        return box != null && !(box.x < 0 && box.z < 1) && !(box.y < 0 && box.w < 1);
    }
    
    public static double getCenterX(Vector4d box) {
        return box.x + (box.z - box.x) / 2;
    }
}
