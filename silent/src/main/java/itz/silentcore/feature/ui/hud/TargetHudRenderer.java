package itz.silentcore.feature.ui.hud;

import itz.silentcore.SilentCore;
import itz.silentcore.feature.event.impl.Render2DEvent;
import itz.silentcore.feature.module.impl.combat.Aura;
import itz.silentcore.feature.ui.hud.drag.DragComponent;
import itz.silentcore.utils.animation.Animation;
import itz.silentcore.utils.animation.Easing;
import itz.silentcore.utils.client.IMinecraft;
import itz.silentcore.utils.math.StopWatch;
import itz.silentcore.utils.render.ColorRGBA;
import itz.silentcore.utils.render.Fonts;
import itz.silentcore.utils.render.RenderContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class TargetHudRenderer extends DragComponent implements IMinecraft {
    private final Animation scaleAnimation = new Animation(650, Easing.BAKEK);
    private final Animation faceAlphaAnimation = new Animation(125, Easing.EXPO_OUT);
    private final StopWatch stopWatch = new StopWatch();
    private final StopWatch distanceUpdateTimer = new StopWatch();
    
    private LivingEntity lastTarget;
    private float health;
    private float absorption;
    private float displayedDistance;

    public TargetHudRenderer() {
        super("TargetHud");
        setX(10);
        setY(80);
        setWidth(120);
        setHeight(41);
        scaleAnimation.reset(0);
        faceAlphaAnimation.reset(0);
    }

    @Override
    public void render(Render2DEvent event) {
        RenderContext ctx = event.getContext();
        
        // Получаем цель из Aura
        Aura auraModule = (Aura) SilentCore.getInstance().moduleManager.getModule("Aura");
        LivingEntity auraTarget = auraModule != null ? auraModule.getTarget() : null;
        
        if (auraTarget != null) {
            lastTarget = auraTarget;
            scaleAnimation.animate(1);
            faceAlphaAnimation.animate(1);
            stopWatch.reset();
        } else if (mc.currentScreen instanceof net.minecraft.client.gui.screen.ChatScreen) {
            lastTarget = mc.player;
            scaleAnimation.animate(1);
            faceAlphaAnimation.animate(1);
            stopWatch.reset();
        } else if (stopWatch.finished(500)) {
            scaleAnimation.animate(0);
            faceAlphaAnimation.animate(0);
        }

        // Обновляем анимации
        scaleAnimation.update();
        faceAlphaAnimation.update();

        // Если анимация закрыта, не рисуем
        if (scaleAnimation.getValue() < 0.01f) {
            return;
        }

        if (lastTarget != null) {
            drawMain(ctx);
            drawArmor(ctx);
            drawFace(ctx);
        }
    }

    private void drawMain(RenderContext ctx) {
        float animValue = scaleAnimation.getValue();
        
        // Вычисляем здоровье с плавной анимацией
        float hp = lastTarget.getHealth();
        health = MathHelper.clamp(lerp(health, hp / lastTarget.getMaxHealth(), 0.1f), 0, 1);
        
        // Вычисляем абсорбцию
        float absorptionAmount = lastTarget.getAbsorptionAmount();
        absorption = MathHelper.clamp(lerp(absorption, absorptionAmount / 20.0f, 0.1f), 0, 1);
        
        // Вычисляем дистанцию
        float actualDistance = mc.player.distanceTo(lastTarget);
        float roundedDistance = Math.round(actualDistance * 2) / 2.0f;
        if (distanceUpdateTimer.finished(10)) {
            displayedDistance = MathHelper.clamp(lerp(displayedDistance, roundedDistance, 0.5f), 0, 100);
            distanceUpdateTimer.reset();
        }
        
        String distanceText = String.format("%.1f", displayedDistance);
        String healthText = String.format("%.1f", hp);
        
        // Размеры
        float baseWidth = 140;
        setWidth((int) baseWidth);
        setHeight(46);

        // Применяем масштаб анимации
        var matrices = ctx.getContext().getMatrices();
        matrices.push();
        matrices.translate(getX() + getWidth() / 2f, getY() + getHeight() / 2f, 0);
        matrices.scale(animValue, animValue, 1);
        matrices.translate(-(getX() + getWidth() / 2f), -(getY() + getHeight() / 2f), 0);

        // Блюр фона
        ctx.drawBlur(getX(), getY(), getWidth(), getHeight() - 5, 8, 12, ColorRGBA.of(255, 255, 255, 255), 0.8f);
        
        // Основной фон с градиентом
        ctx.drawRect(getX(), getY(), getWidth(), getHeight() - 5, 8,
                ColorRGBA.of(18, 19, 20, (int)(180 * animValue)),
                ColorRGBA.of(10, 11, 12, (int)(180 * animValue)),
                ColorRGBA.of(10, 11, 12, (int)(180 * animValue)),
                ColorRGBA.of(18, 19, 20, (int)(180 * animValue)));
        
        // Обводка
        ColorRGBA primaryColor = itz.silentcore.utils.client.ClientUtility.getThemePrimaryColorRGBA();
        ctx.drawBorder(getX(), getY(), getWidth(), getHeight() - 5, 8, 1f,
                ColorRGBA.of(primaryColor.getR(), primaryColor.getG(), primaryColor.getB(), (int)(100 * animValue)));

        // Круг здоровья
        float arcX = getX() + getWidth() - 32f;
        float arcY = getY() + 4f;
        float arcSize = 32;
        
        // Фон круга (полный круг)
        ctx.drawBorder(arcX, arcY, arcSize, arcSize, arcSize / 2f, 2.5f,
                ColorRGBA.of(255, 255, 255, (int)(30 * animValue)));
        
        // Здоровье (дуга)
        drawHealthArc(ctx, arcX + arcSize / 2f, arcY + arcSize / 2f, arcSize / 2f - 1.25f, health * 360, 
                ColorRGBA.of(primaryColor.getR(), primaryColor.getG(), primaryColor.getB(), (int)(255 * animValue)), 2.5f);
        
        // Абсорбция (дуга поверх)
        if (absorption > 0) {
            drawHealthArc(ctx, arcX + arcSize / 2f, arcY + arcSize / 2f, arcSize / 2f - 1.25f, absorption * 360,
                    ColorRGBA.of(255, 215, 0, (int)(255 * animValue)), 2.5f);
        }

        // Текст имени
        String name = lastTarget.getName().getString();
        ctx.drawText(name, Fonts.sf_pro, getX() + 32, getY() + 10f, 10, 0.04f,
                ColorRGBA.of(255, 255, 255, (int)(255 * animValue)));
        
        // Текст дистанции
        ctx.drawText("Distance: " + distanceText, Fonts.sf_pro, getX() + 32, getY() + 22f, 8, 0.04f,
                ColorRGBA.of(200, 200, 200, (int)(200 * animValue)));

        // Текст здоровья в центре круга
        float arcCenterX = arcX + arcSize / 2f;
        float arcCenterY = arcY + arcSize / 2f;
        float healthTextWidth = Fonts.sf_pro.getWidth(healthText, 9);
        ctx.drawText(healthText, Fonts.sf_pro, arcCenterX - healthTextWidth / 2f, arcCenterY - 4, 9, 0.04f,
                ColorRGBA.of(255, 255, 255, (int)(255 * animValue)));

        matrices.pop();
    }

    private void drawArmor(RenderContext ctx) {
        float animValue = scaleAnimation.getValue();
        
        ItemStack[] slots = new ItemStack[] {
                lastTarget.getMainHandStack(),
                lastTarget.getOffHandStack(),
                lastTarget.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD),
                lastTarget.getEquippedStack(net.minecraft.entity.EquipmentSlot.CHEST),
                lastTarget.getEquippedStack(net.minecraft.entity.EquipmentSlot.LEGS),
                lastTarget.getEquippedStack(net.minecraft.entity.EquipmentSlot.FEET)
        };
        
        float x = getX() + 24f;
        float y = getY() + 28;
        float slotSize = 12;
        
        var matrices = ctx.getContext().getMatrices();
        matrices.push();
        
        for (int i = 0; i < 6; i++) {
            float currentX = x + i * 14f;
            float currentY = y;
            
            // Фон слота с блюром
            ctx.drawBlur(currentX, currentY, slotSize, slotSize, 3, 8, ColorRGBA.of(255, 255, 255, 255), 0.8f);
            ctx.drawRect(currentX, currentY, slotSize, slotSize, 3,
                    ColorRGBA.of(18, 19, 20, (int)(150 * animValue)));
            
            // Обводка слота
            ColorRGBA primaryColor = itz.silentcore.utils.client.ClientUtility.getThemePrimaryColorRGBA();
            ctx.drawBorder(currentX, currentY, slotSize, slotSize, 3, 0.5f,
                    ColorRGBA.of(primaryColor.getR(), primaryColor.getG(), primaryColor.getB(), (int)(80 * animValue)));
            
            // Предмет или крестик
            if (!slots[i].isEmpty()) {
                DrawContext drawContext = ctx.getContext();
                matrices.push();
                matrices.translate(currentX + 2, currentY + 2, 0);
                matrices.scale(0.5f, 0.5f, 1);
                drawContext.drawItem(slots[i], 0, 0);
                matrices.pop();
            } else {
                // Крестик по центру
                float centerX = currentX + slotSize / 2f;
                float centerY = currentY + slotSize / 2f;
                float xWidth = Fonts.sf_pro.getWidth("×", 10);
                ctx.drawText("×", Fonts.sf_pro, centerX - xWidth / 2f, centerY - 4, 10, 0.04f,
                        ColorRGBA.of(150, 150, 150, (int)(150 * animValue)));
            }
        }
        
        matrices.pop();
    }

    private void drawFace(RenderContext ctx) {
        EntityRenderer<? super LivingEntity, ?> baseRenderer = mc.getEntityRenderDispatcher().getRenderer(lastTarget);
        if (!(baseRenderer instanceof LivingEntityRenderer)) {
            return;
        }
        
        @SuppressWarnings("unchecked")
        LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?> renderer = 
                (LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>) baseRenderer;
        
        LivingEntityRenderState state = renderer.getAndUpdateRenderState(lastTarget, mc.getRenderTickCounter().getTickDelta(false));
        Identifier textureLocation = renderer.getTexture(state);
        
        float alpha = faceAlphaAnimation.getValue();
        
        if (alpha > 0.01f) {
            // Рисуем голову игрока
            DrawContext drawContext = ctx.getContext();
            float faceX = getX() + 6;
            float faceY = getY() + 6f;
            float faceSize = 20;
            
            // Фон для головы
            ctx.drawRect(faceX - 1, faceY - 1, faceSize + 2, faceSize + 2, 4,
                    ColorRGBA.of(0, 0, 0, (int)(100 * alpha)));
            
            // Рисуем текстуру головы (8x8 пикселей из скина, начиная с u=8, v=8)
            // Используем правильный метод drawTexture
            var matrices = drawContext.getMatrices();
            matrices.push();
            matrices.translate(faceX, faceY, 0);
            
            // Основной слой головы
            drawContext.drawTexture(RenderLayer::getGuiTextured, textureLocation, 
                    0, 0, 8, 8, 8, 8, 64, 64, 
                    ColorRGBA.of(255, 255, 255, (int)(255 * alpha)).getRGB());
            
            // Второй слой головы (шлем/волосы)
            drawContext.drawTexture(RenderLayer::getGuiTextured, textureLocation, 
                    0, 0, 40, 8, 8, 8, 64, 64, 
                    ColorRGBA.of(255, 255, 255, (int)(255 * alpha)).getRGB());
            
            matrices.pop();
        }
    }

    // Улучшенная отрисовка дуги здоровья
    private void drawHealthArc(RenderContext ctx, float centerX, float centerY, float radius, float angle, ColorRGBA color, float thickness) {
        if (angle <= 0) return;
        
        int segments = Math.max(32, (int)(angle / 5)); // Много сегментов для плавности
        float angleStep = angle / segments;
        
        for (int i = 0; i < segments; i++) {
            float startAngle = (float)Math.toRadians(i * angleStep - 90); // -90 чтобы начать сверху
            float endAngle = (float)Math.toRadians((i + 1) * angleStep - 90);
            
            // Внешний радиус
            float outerRadius = radius + thickness / 2f;
            // Внутренний радиус
            float innerRadius = radius - thickness / 2f;
            
            // Точки внешнего круга
            float x1Outer = centerX + (float)Math.cos(startAngle) * outerRadius;
            float y1Outer = centerY + (float)Math.sin(startAngle) * outerRadius;
            float x2Outer = centerX + (float)Math.cos(endAngle) * outerRadius;
            float y2Outer = centerY + (float)Math.sin(endAngle) * outerRadius;
            
            // Точки внутреннего круга
            float x1Inner = centerX + (float)Math.cos(startAngle) * innerRadius;
            float y1Inner = centerY + (float)Math.sin(startAngle) * innerRadius;
            float x2Inner = centerX + (float)Math.cos(endAngle) * innerRadius;
            float y2Inner = centerY + (float)Math.sin(endAngle) * innerRadius;
            
            // Рисуем сегмент как маленький прямоугольник
            float midX = (x1Outer + x2Outer + x1Inner + x2Inner) / 4f;
            float midY = (y1Outer + y2Outer + y1Inner + y2Inner) / 4f;
            ctx.drawRect(midX - thickness / 2f, midY - thickness / 2f, thickness, thickness, thickness / 2f, color);
        }
    }

    private float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }
}
