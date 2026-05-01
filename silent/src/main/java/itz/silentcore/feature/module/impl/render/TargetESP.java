package itz.silentcore.feature.module.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import itz.silentcore.feature.event.impl.TickEvent;
import itz.silentcore.feature.event.impl.WorldRenderEvent;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.feature.module.api.setting.impl.BooleanSetting;
import itz.silentcore.feature.module.api.setting.impl.ColorSetting;
import itz.silentcore.feature.module.api.setting.impl.ModeSetting;
import itz.silentcore.feature.module.impl.combat.Aura;
import itz.silentcore.feature.theme.ThemeManager;
import itz.silentcore.utils.animation.Animation;
import itz.silentcore.utils.animation.Easing;
import itz.silentcore.utils.render.ColorRGBA;
import itz.silentcore.utils.render.ColorUtils;
import itz.silentcore.utils.render.Render3D;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4i;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@ModuleAnnotation(name = "TargetESP", category = Category.RENDER, description = "Highlights target entity")
public class TargetESP extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Cube", "Ghosts");
    private final ModeSetting cubeType = new ModeSetting("Cube Type", "1", "2", "3", "4");
    private final BooleanSetting showOnHover = new BooleanSetting("Show On Hover", true);
    private final BooleanSetting useClientColor = new BooleanSetting("Client Color", true);
    private final ColorSetting customColor = new ColorSetting("Custom Color", new ColorRGBA(255, 101, 57, 255), 
        () -> !useClientColor.isEnabled(), () -> new ColorRGBA(255, 101, 57, 255));

    private final Animation espAnimation = new Animation(400, Easing.CUBIC_OUT);
    private LivingEntity lastTarget = null;
    
    // Простая плавная анимация вращения куба
    private float cubeRotation = 0f;
    
    // Плавная интерполяция размера куба
    private float prevCubeSize = 0.0f;

    @Subscribe
    public void onTick(TickEvent event) {
        // Медленное плавное вращение куба - 1.5 градуса за тик
        cubeRotation += 1.5f;
        if (cubeRotation >= 360f) {
            cubeRotation -= 360f;
        }
    }

    @Subscribe
    public void onWorldRender(WorldRenderEvent event) {
        if (mc.player == null || mc.world == null) return;

        LivingEntity currentTarget = null;
        
        // Get target from Aura if enabled
        if (Aura.getInstance() != null && Aura.getInstance().isEnabled()) {
            currentTarget = Aura.getInstance().getTarget();
        }

        // Show on hover if enabled
        if (showOnHover.isEnabled() && currentTarget == null) {
            // Check if player is looking at an entity
            if (mc.targetedEntity instanceof LivingEntity living) {
                currentTarget = living;
            }
        }

        // Update last target
        if (currentTarget != null) {
            lastTarget = currentTarget;
        }

        // Only render if we have a target
        if (lastTarget != null && currentTarget != null) {
            // Update animation
            espAnimation.setTarget(1);
            espAnimation.update();
            float anim = espAnimation.getValue();
            
            float red = MathHelper.clamp((lastTarget.hurtTime - mc.getRenderTickCounter().getTickDelta(false)) / 20, 0, 1);
            
            String modeValue = mode.get();
            if ("Cube".equals(modeValue)) {
                drawCube(lastTarget, anim, red, cubeType.get());
            } else if ("Ghosts".equals(modeValue)) {
                drawGhosts(lastTarget, anim, red, 0.62F);
            }
        } else {
            // Fade out animation when target is lost
            espAnimation.setTarget(0);
            espAnimation.update();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        lastTarget = null;
        cubeRotation = 0f;
        prevCubeSize = 0f;
    }

    private Vec3d lerpPosition(Entity entity) {
        float tickDelta = mc.getRenderTickCounter().getTickDelta(false);
        return new Vec3d(
            MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX()),
            MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY()),
            MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ())
        );
    }

    private int getBaseColor() {
        if (useClientColor.isEnabled()) {
            return ThemeManager.getInstance().getPrimaryColor();
        }
        return customColor.getIntColor();
    }

    private void drawCube(LivingEntity target, float anim, float red, String png) {
        float baseSize = red - anim - 0.17F;
        float targetSize = baseSize;

        if (png != null) {
            switch (png) {
                case "2" -> targetSize = red - anim - 0.05F;
                case "4" -> targetSize = red - anim + 0.05F;
            }
        }

        // Очень плавная интерполяция размера
        float size = MathHelper.lerp(0.15f, prevCubeSize, targetSize);
        prevCubeSize = size;

        Camera camera = mc.gameRenderer.getCamera();
        Vec3d vec = lerpPosition(target).subtract(camera.getPos());
        
        // Добавляем tickDelta для плавности между кадрами
        float tickDelta = mc.getRenderTickCounter().getTickDelta(false);
        float smoothRotation = cubeRotation + (1.5f * tickDelta);
        
        MatrixStack matrix = new MatrixStack();
        matrix.push();
        matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
        matrix.translate(vec.x, vec.y + target.getBoundingBox().getLengthY() / 2, vec.z);
        matrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        
        // Плавное вращение
        matrix.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(smoothRotation));

        MatrixStack.Entry entry = matrix.peek();
        
        // При ударе - красный, иначе - цвет клиента
        int baseColor = red > 0 ? new Color(255, 0, 0).getRGB() : getBaseColor();
        int finalColor = ColorUtils.setAlpha(baseColor, (int)(anim * 255));
        
        Render3D.drawTexture(
            entry,
            Identifier.of("silentcore:textures/esp/capture" + png + ".png"),
            -size / 2,
            -size / 2,
            size,
            size,
            new Vector4i(finalColor),
            false
        );
        matrix.pop();
    }


    private void drawGhosts(LivingEntity target, float anim, float red, float speed) {
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d targetPos = lerpPosition(target).subtract(camera.getPos());
        boolean canSee = mc.player.canSee(target);
        
        // Плавная интерполяция времени между кадрами для устранения рывков
        float tickDelta = mc.getRenderTickCounter().getTickDelta(false);
        double iAge = mc.player.age + tickDelta; // Добавляем tickDelta для плавности
        
        float halfHeight = target.getHeight() / 2 + 0.2F;
        float baseWidth = target.getWidth() + 0.2f;
        float minY = 0.2f;
        float maxY = target.getHeight() - 0.2F;

        float hitEffect = Math.min(red * 2f, 2f);
        float acceleration = (float) Math.sin(hitEffect * Math.PI) * 0.18f;
        float bany = (float) Math.sin(hitEffect * Math.PI) * -0.04f;

        for (int j = 0; j < 4; j++) {
            for (int i = 0, length = (int) 10.3f; i <= length; i++) {
                // Плавное вращение с tickDelta
                double baseAngle = ((i / 2F + iAge * speed * 2.0f) * length + (j * 90)) % (length * 180);
                double radians = Math.toRadians(baseAngle);

                float heightOffset = 0f;
                float radiusOffset = 1.04f;
                float distanceMultiplier = 1.0f + acceleration;

                // Плавная синусоида для вертикального движения с tickDelta
                double sinQuad = Math.sin(Math.toRadians(iAge * 0.7 + i * (j + halfHeight)) * 1.1) / 2;
                double adjustedSin = (j % 2 == 0) ? sinQuad : -sinQuad;
                double yOffset = minY + (adjustedSin + 0.5) * (maxY - minY) + heightOffset;
                float offset = ((float) (i + length) / (length + length));

                MatrixStack matrices = new MatrixStack();
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

                double finalWidth = baseWidth * distanceMultiplier * radiusOffset;
                matrices.translate(targetPos.x + Math.cos(radians) * finalWidth, targetPos.y + yOffset, targetPos.z + Math.sin(radians) * finalWidth);

                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                MatrixStack.Entry entry = matrices.peek();
                
                int baseColor = red > 0 ? new Color(255, 0, 0).getRGB() : getBaseColor();
                int finalColor = ColorUtils.setAlpha(baseColor, (int)(offset * anim * 255));
                
                float scale = 0.6f * offset * (0.6f + speed * 0.1f) + bany;
                Render3D.drawTexture(
                    entry,
                    Identifier.of("silentcore:textures/particles/bloom.png"),
                    -scale / 2,
                    -scale / 2,
                    scale,
                    scale,
                    new Vector4i(finalColor),
                    canSee
                );
            }
        }
    }



}
