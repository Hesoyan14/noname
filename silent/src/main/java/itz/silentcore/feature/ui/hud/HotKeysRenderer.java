package itz.silentcore.feature.ui.hud;
import itz.silentcore.SilentCore;
import itz.silentcore.feature.event.impl.Render2DEvent;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.ui.hud.drag.DragComponent;
import itz.silentcore.utils.animation.Animation;
import itz.silentcore.utils.animation.Easing;
import itz.silentcore.utils.client.ClientUtility;
import itz.silentcore.utils.render.ColorRGBA;
import itz.silentcore.utils.client.Keyboard;
import org.lwjgl.glfw.GLFW;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static itz.silentcore.utils.render.Fonts.icons;
import static itz.silentcore.utils.render.Fonts.sf_pro;

public class HotKeysRenderer extends DragComponent {

    private final Map<Module, Animation> moduleAnimations = new HashMap<>();
    private float smoothedHeight = 0f;

    public HotKeysRenderer() {
        super("hotkeys");
        setDraggable(true);
        setAllowDragX(true);
        setAllowDragY(true);
        setX(30);
        setY(30);
    }

    @Override
    public void render(Render2DEvent event) {
        var context = event.getContext();

        float x = getX();
        float y = getY();
        float paddingX = 6f;
        float paddingY = 4f;
        float radius = 5.0f;
        float textSize = 8.0f;
        float iconSize = textSize + 2.0f;
        float gap = 4f;
        float lineSpacing = 2f;

        List<Module> boundModules = getModulesWithKeybinds();

        for (Module module : SilentCore.getInstance().moduleManager.getModules()) {
            if (!moduleAnimations.containsKey(module)) {
                moduleAnimations.put(module, new Animation(800, Easing.EXPO_OUT));
            }

            Animation anim = moduleAnimations.get(module);
            boolean shouldShow = boundModules.contains(module);

            float currentValue = anim.getValue();
            float targetValue = shouldShow ? 1.0f : 0.0f;

            if ((currentValue > 0.5f && !shouldShow) || (currentValue < 0.5f && shouldShow)) {
                anim = new Animation(800, Easing.EXPO_OUT);
                moduleAnimations.put(module, anim);
            }

            anim.animate(targetValue);
            anim.update();
        }

        moduleAnimations.entrySet().removeIf(entry ->
                entry.getValue().getValue() <= 0.01 && !boundModules.contains(entry.getKey())
        );

        String headerIconText = "k";
        float headerIconWidth = icons.getWidth(headerIconText, iconSize);
        String headerText = "HotKeys";
        float headerTextWidth = sf_pro.getWidth(headerText, textSize);

        float sfHeight = sf_pro.getMetrics().lineHeight() * textSize + 3;
        float iconHeight = icons.getMetrics().lineHeight() * iconSize;
        float lineHeight = Math.max(sfHeight, iconHeight);

        float headerWidth = headerIconWidth + gap + headerTextWidth;
        float maxWidth = headerWidth + 25;

        for (Module module : boundModules) {
            Animation anim = moduleAnimations.get(module);
            if (anim == null || anim.getValue() <= 0.01) continue;

            String modIconText = module.getCategory().getIcon();
            float modIconWidth = icons.getWidth(modIconText, iconSize);

            String modName = module.getName();
            float modNameWidth = sf_pro.getWidth(modName, textSize);

            int keyCode = module.getKey();
            String keyText = (keyCode > 0 && keyCode != GLFW.GLFW_KEY_UNKNOWN) ? Keyboard.getKeyName(keyCode) : "?";
            float keyTextWidth = sf_pro.getWidth(keyText, textSize);

            float lineWidth = modIconWidth + gap + modNameWidth + gap + keyTextWidth;
            maxWidth = Math.max(maxWidth, lineWidth);
        }

        float totalWidth = maxWidth + paddingX * 2f + 5;

        float currentY = paddingY;
        currentY += lineHeight + lineSpacing;

        List<ModuleRenderData> renderData = new ArrayList<>();
        for (Module module : boundModules) {
            Animation anim = moduleAnimations.get(module);
            if (anim == null || anim.getValue() <= 0.01) continue;

            float animValue = (float) anim.getValue();
            renderData.add(new ModuleRenderData(module, animValue, currentY));
            currentY += (lineHeight + lineSpacing) * animValue;
        }

        float targetHeight = currentY + paddingY - lineSpacing;
        if (renderData.isEmpty()) {
            targetHeight = lineHeight + paddingY * 2f;
        }

        smoothedHeight += (targetHeight - smoothedHeight) * 0.15f;

        boolean rotateSelf = itz.silentcore.feature.ui.hud.drag.DragComponent.getDragging() == this;
        if (rotateSelf) {
            var matrices2 = context.getContext().getMatrices();
            matrices2.push();
            matrices2.translate((x + totalWidth / 2f), (y + smoothedHeight / 2f), 0);
            matrices2.multiply(new Quaternionf().rotateZ((float) Math.toRadians(getRotation())));
            matrices2.translate(-(x + totalWidth / 2f), -(y + smoothedHeight / 2f), 0);
        }


        context.drawBlur(x, y, totalWidth, smoothedHeight, radius, 30, ColorRGBA.of(255, 255, 255, 255));
        context.drawRect(x, y, totalWidth, smoothedHeight, radius, ColorRGBA.of(0, 0, 0, 166));

        float cursorX = x + paddingX;
        float cursorY = y + paddingY;
        float centerY = cursorY + lineHeight / 2f;
        float iconY = centerY - iconHeight / 2f;
        float sfY = centerY - sfHeight / 2f;

        ColorRGBA themeColor = ClientUtility.getThemePrimaryColorRGBA();
        context.drawText(headerIconText, icons, cursorX, iconY, iconSize, themeColor);
        cursorX += headerIconWidth + gap;
        context.drawText(headerText, sf_pro, cursorX, sfY, textSize, ColorRGBA.of(255, 255, 255));

        for (ModuleRenderData data : renderData) {
            cursorX = x + paddingX;
            cursorY = y + data.yOffset;
            centerY = cursorY + lineHeight / 2f;
            iconY = centerY - iconHeight / 2f;
            sfY = centerY - sfHeight / 2f;

            float animValue = data.animValue;

            float scale = 0.85f + (animValue * 0.15f);

            float offsetX = -5f * (1f - animValue);

            int alpha = (int) (255 * animValue);

            ColorRGBA moduleIconColor = themeColor.withAlpha(alpha);
            ColorRGBA textColor = ColorRGBA.of(255, 255, 255, alpha);

            String modIconText = data.module.getCategory().getIcon();
            float modIconWidth = icons.getWidth(modIconText, iconSize);
            context.drawText(modIconText, icons, cursorX + offsetX, iconY, iconSize, moduleIconColor);
            cursorX += modIconWidth + gap;

            String modName = data.module.getName();
            float modNameWidth = sf_pro.getWidth(modName, textSize);
            context.drawText(modName, sf_pro, cursorX + offsetX, sfY, textSize, textColor);
            cursorX += modNameWidth + gap;

            int keyCode = data.module.getKey();
            String keyText = (keyCode > 0 && keyCode != GLFW.GLFW_KEY_UNKNOWN) ? 
                "[" + Keyboard.getKeyName(keyCode) + "]" : "[?]";
            context.drawText(keyText, sf_pro, cursorX + offsetX, sfY, textSize, textColor);
        }


        setWidth(totalWidth);
        setHeight(smoothedHeight);

        if (rotateSelf) {
            var matrices2 = context.getContext().getMatrices();
            matrices2.pop();
        }
    }

    private List<Module> getModulesWithKeybinds() {
        List<Module> result = new ArrayList<>();
        for (Module module : SilentCore.getInstance().moduleManager.getModules()) {
            int k = module.getKey();
            if (module.isEnabled() && k != GLFW.GLFW_KEY_UNKNOWN && k > 0) {
                result.add(module);
            }
        }
        return result;
    }

    private static class ModuleRenderData {
        final Module module;
        final float animValue;
        final float yOffset;

        ModuleRenderData(Module module, float animValue, float yOffset) {
            this.module = module;
            this.animValue = animValue;
            this.yOffset = yOffset;
        }
    }
}



