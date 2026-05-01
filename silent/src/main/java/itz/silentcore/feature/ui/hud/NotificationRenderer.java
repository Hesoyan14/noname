package itz.silentcore.feature.ui.hud;

import itz.silentcore.feature.event.impl.Render2DEvent;
import itz.silentcore.feature.ui.hud.drag.DragComponent;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.utils.animation.Animation;
import itz.silentcore.utils.animation.Easing;
import itz.silentcore.utils.render.ColorRGBA;
import itz.silentcore.utils.client.ClientUtility;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static itz.silentcore.utils.render.Fonts.sf_pro;
import static itz.silentcore.utils.render.Fonts.icons;

public class NotificationRenderer extends DragComponent {
    private static NotificationRenderer INSTANCE;

    private static class Entry {
        String text;
        static class TextPart {
            String text;
            ColorRGBA color;
            TextPart(String text, ColorRGBA color) { this.text = text; this.color = color; }
        }
        List<TextPart> parts;
        String icon;
        ColorRGBA iconColor;
        long start;
        long duration;
        float animatedIndexY;
        boolean initAnimatedY = false;
        final Animation tabAnimation = new Animation(250, Easing.BAKEK_SIZE);

        Entry(String text, long duration) {
            this.text = text;
            this.start = System.currentTimeMillis();
            this.duration = duration;
            tabAnimation.animate(1);
        }
        Entry(List<TextPart> parts, long duration) {
            this.parts = parts;
            this.start = System.currentTimeMillis();
            this.duration = duration;
            tabAnimation.animate(1);
        }
        Entry(List<TextPart> parts, String icon, ColorRGBA iconColor, long duration) {
            this.parts = parts;
            this.icon = icon;
            this.iconColor = iconColor;
            this.start = System.currentTimeMillis();
            this.duration = duration;
            tabAnimation.animate(1);
        }
    }

    private final List<Entry> entries = new ArrayList<>();
    private final long defaultDuration = 2000;
    private float animatedBaseY = 200f;

    public NotificationRenderer() {
        super("notifications");
        INSTANCE = this;
        setDraggable(true);
        setAllowDragX(false);
        setAllowDragY(true);
        setY(200);
        animatedBaseY = 200f;
    }

    public static void push(String message) {
        if (INSTANCE == null) return;
        if (INSTANCE.entries.size() >= 15) {
            INSTANCE.entries.remove(0);
        }
        INSTANCE.entries.add(new Entry(message, INSTANCE.defaultDuration));
    }

    public static void pushWithIcon(String message, String icon, ColorRGBA iconColor) {
        if (INSTANCE == null) return;
        if (INSTANCE.entries.size() >= 15) {
            INSTANCE.entries.remove(0);
        }
        List<Entry.TextPart> parts = new ArrayList<>();
        parts.add(new Entry.TextPart(message, ColorRGBA.of(255, 255, 255)));
        INSTANCE.entries.add(new Entry(parts, icon, iconColor, INSTANCE.defaultDuration));
    }

    public static void pushModuleEnabled(String moduleName) {
        if (INSTANCE == null) return;
        if (INSTANCE.entries.size() >= 15) {
            INSTANCE.entries.remove(0);
        }
        List<Entry.TextPart> parts = new ArrayList<>();
        parts.add(new Entry.TextPart("Модуль ", ColorRGBA.of(255, 255, 255)));
        parts.add(new Entry.TextPart(moduleName, ClientUtility.getThemePrimaryColorRGBA()));
        parts.add(new Entry.TextPart(" включен", ColorRGBA.of(255, 255, 255)));
        String icon = "P";
        ColorRGBA iconColor = ClientUtility.getThemePrimaryColorRGBA();
        INSTANCE.entries.add(new Entry(parts, icon, iconColor, INSTANCE.defaultDuration));
    }

    public static void pushModuleDisabled(String moduleName) {
        if (INSTANCE == null) return;
        if (INSTANCE.entries.size() >= 15) {
            INSTANCE.entries.remove(0);
        }
        List<Entry.TextPart> parts = new ArrayList<>();
        parts.add(new Entry.TextPart("Модуль ", ColorRGBA.of(255, 255, 255)));
        parts.add(new Entry.TextPart(moduleName, ClientUtility.getThemePrimaryColorRGBA()));
        parts.add(new Entry.TextPart(" выключен", ColorRGBA.of(255, 255, 255)));
        String icon = "P";
        ColorRGBA iconColor = ClientUtility.getThemePrimaryColorRGBA();
        INSTANCE.entries.add(new Entry(parts, icon, iconColor, INSTANCE.defaultDuration));
    }

    public static void pushModuleEnabled(Module module) {
        if (INSTANCE == null || module == null) return;
        if (INSTANCE.entries.size() >= 15) {
            INSTANCE.entries.remove(0);
        }
        List<Entry.TextPart> parts = new ArrayList<>();

        // rgba(52, 166, 46, 1)

        parts.add(new Entry.TextPart("Модуль ", ColorRGBA.of(255, 255, 255)));
        parts.add(new Entry.TextPart(module.getName(), ClientUtility.getThemePrimaryColorRGBA()));
        parts.add(new Entry.TextPart(" включен", ColorRGBA.of(255, 255, 255)));

        // parts.add(new Entry.TextPart("Модуль ", ColorRGBA.of(255, 255, 255)));
        // parts.add(new Entry.TextPart(module.getName(), ColorRGBA.of(165, 129, 238)));
        // parts.add(new Entry.TextPart(" включен", ColorRGBA.of(255, 255, 255)));

        String icon = module.getCategory() != null ? module.getCategory().getIcon() : "P";
        ColorRGBA iconColor = ClientUtility.getThemePrimaryColorRGBA();
        INSTANCE.entries.add(new Entry(parts, icon, iconColor, INSTANCE.defaultDuration));
    }

    public static void pushModuleDisabled(Module module) {
        if (INSTANCE == null || module == null) return;
        if (INSTANCE.entries.size() >= 15) {
            INSTANCE.entries.remove(0);
        }
        List<Entry.TextPart> parts = new ArrayList<>();

        parts.add(new Entry.TextPart("Модуль ", ColorRGBA.of(255, 255, 255)));
        parts.add(new Entry.TextPart(module.getName(), ClientUtility.getThemePrimaryColorRGBA()));
        parts.add(new Entry.TextPart(" выключен", ColorRGBA.of(255, 255, 255)));

        // parts.add(new Entry.TextPart("Модуль ", ColorRGBA.of(255, 255, 255)));
        // parts.add(new Entry.TextPart(module.getName(), ColorRGBA.of(165, 129, 238)));
        // parts.add(new Entry.TextPart(" включен", ColorRGBA.of(255, 255, 255)));

        String icon = module.getCategory() != null ? module.getCategory().getIcon() : "P";
        ColorRGBA iconColor = ClientUtility.getThemePrimaryColorRGBA();
        INSTANCE.entries.add(new Entry(parts, icon, iconColor, INSTANCE.defaultDuration));
    }

    @Override
    public void render(Render2DEvent event) {
        var context = event.getContext();

        float paddingX = 8f;
        float paddingY = 4f;
        float radius = 5.0f;
        float textSize = 8.0f;
        float iconGap = 0.5f;
        float spacing = 3f;

        float centerX = context.getContext().getScaledWindowWidth() / 2f;
        float targetBaseY = getY() == 0 ? (context.getContext().getScaledWindowHeight() / 2f + 10f) : getY();
        animatedBaseY += (targetBaseY - animatedBaseY) * 0.25f;

        long now = System.currentTimeMillis();
        Iterator<Entry> it = entries.iterator();
        while (it.hasNext()) {
            Entry e = it.next();
            if (now - e.start >= e.duration) {
                it.remove();
            }
        }

        boolean chatOpen = MinecraftClient.getInstance().currentScreen instanceof ChatScreen;

        if (entries.isEmpty()) {
            String sample = "Пример уведомления";
            float textWidth = sf_pro.getWidth(sample, textSize);
            float iconSize = textSize + 1.5f;
            float iconWidth = icons.getWidth("P", iconSize);
            float textHeight = sf_pro.getMetrics().lineHeight() * textSize;
            float iconHeight = icons.getMetrics().lineHeight() * iconSize;
            float examplePaddingX = paddingX;
            float width = textWidth + iconWidth + iconGap + examplePaddingX * 2;
            float height = paddingY * 2 + Math.max(textHeight, iconHeight);
            float x = centerX - width / 2f;
            float y = animatedBaseY;

            if (chatOpen) {
                boolean rotateSelf = itz.silentcore.feature.ui.hud.drag.DragComponent.getDragging() == this;
                if (rotateSelf) {
                    var matrices = context.getContext().getMatrices();
                    matrices.push();
                    matrices.translate((x + width / 2f), (y + height / 2f), 0);
                    matrices.multiply(new Quaternionf().rotateZ((float) Math.toRadians(getRotation())));
                    matrices.translate(-(x + width / 2f), -(y + height / 2f), 0);
                }
                context.drawBlur(x, y, width, height, radius, 30, ColorRGBA.of(255, 255, 255, 255));
                context.drawRect(x, y, width, height, radius, ColorRGBA.of(0, 0, 0, 166));
                float contentWidth = iconWidth + iconGap + textWidth;
                float contentStartX = x + examplePaddingX;
                float textBaselineY = y + (height - textHeight) / 2f;
                float iconBaselineY = y + (height - iconHeight) / 2f;
                float iconOffsetLeft = 2.0f;
                context.drawText("P", icons, contentStartX - iconOffsetLeft, iconBaselineY, iconSize, ClientUtility.getThemePrimaryColorRGBA());
                context.drawText(sample, sf_pro, contentStartX + iconWidth + iconGap, textBaselineY, textSize, ColorRGBA.of(255, 255, 255));
                if (rotateSelf) {
                    var matrices = context.getContext().getMatrices();
                    matrices.pop();
                }
            }

            setX(x);
            setWidth(width);
            setHeight(height);
            return;
        }

        int index = 0;
        float maxWidth = 0f;

        boolean rotateSelf = itz.silentcore.feature.ui.hud.drag.DragComponent.getDragging() == this;
        if (rotateSelf) {
            float panelCenterX = getX() + getWidth() / 2f;
            float sampleHeight = paddingY * 2 + sf_pro.getMetrics().lineHeight() * textSize;
            float panelCenterY = animatedBaseY + (Math.max(1, entries.size()) * (sampleHeight + spacing)) / 2f;
            var matrices = context.getContext().getMatrices();
            matrices.push();
            matrices.translate(panelCenterX, panelCenterY, 0);
            matrices.multiply(new Quaternionf().rotateZ((float) Math.toRadians(getRotation())));
            matrices.translate(-panelCenterX, -panelCenterY, 0);
        }

        for (Entry e : entries) {
            e.tabAnimation.update();

            float animScale = e.tabAnimation.getValue();

            float textWidth;
            if (e.parts != null && !e.parts.isEmpty()) {
                float sum = 0f;
                for (Entry.TextPart p : e.parts) sum += sf_pro.getWidth(p.text, textSize);
                textWidth = sum;
            } else {
                textWidth = sf_pro.getWidth(e.text, textSize);
            }
            float textHeight = sf_pro.getMetrics().lineHeight() * textSize;
            float iconSize = textSize + 1.5f;
            float iconWidth = (e.icon != null) ? icons.getWidth(e.icon, iconSize) : 0f;
            float iconHeight = icons.getMetrics().lineHeight() * iconSize;
            float width = paddingX * 2 + (e.icon != null ? (iconWidth + iconGap) : 0f) + textWidth;
            float height = paddingY * 2 + (e.icon != null ? Math.max(textHeight, iconHeight) : textHeight);
            maxWidth = Math.max(maxWidth, width);

            float x = centerX - width / 2f;

            float targetIndexY = index * (height + spacing);
            if (!e.initAnimatedY) {
                e.animatedIndexY = targetIndexY;
                e.initAnimatedY = true;
            } else {
                e.animatedIndexY += (targetIndexY - e.animatedIndexY) * 0.2f;
            }

            float y = animatedBaseY + e.animatedIndexY;

            float scaledWidth = width * animScale;
            float scaledHeight = height * animScale;
            float scaledX = x + (width - scaledWidth) / 2f;
            float scaledY = y + (height - scaledHeight) / 2f;

            context.drawBlur(scaledX, scaledY, scaledWidth, scaledHeight, radius, 30, ColorRGBA.of(255, 255, 255, (int) (255 * animScale)));
            context.drawRect(scaledX, scaledY, scaledWidth, scaledHeight, radius, ColorRGBA.of(0, 0, 0, (int) (166 * animScale)));

            float contentStartX = scaledX + paddingX * animScale;
            float textBaselineY = scaledY + (scaledHeight - textHeight * animScale) / 2f;
            if (e.icon != null) {
                float iconBaselineY = scaledY + (scaledHeight - iconHeight * animScale) / 2f;
                float iconOffsetLeft = 2.0f;
                context.drawText(e.icon, icons, contentStartX - iconOffsetLeft, iconBaselineY, iconSize * animScale,
                        e.iconColor);
                contentStartX += iconWidth * animScale + iconGap * animScale;
            } else {
                contentStartX = scaledX + (scaledWidth - textWidth * animScale) / 2f;
            }

            if (e.parts != null && !e.parts.isEmpty()) {
                float offset = 0f;
                for (Entry.TextPart p : e.parts) {
                    context.drawText(p.text, sf_pro, contentStartX + offset, textBaselineY, textSize * animScale,
                            ColorRGBA.of(p.color.getR(), p.color.getG(), p.color.getB(), 255));
                    offset += sf_pro.getWidth(p.text, textSize * animScale);
                }
            } else {
                context.drawText(e.text, sf_pro, contentStartX, textBaselineY, textSize * animScale,
                        ColorRGBA.of(255, 255, 255, 255));
            }

            index++;
        }

        setX(centerX - maxWidth / 2f);
        setWidth(maxWidth);
        float sampleHeight = paddingY * 2 + sf_pro.getMetrics().lineHeight() * textSize;
        setHeight(index * (sampleHeight + spacing));

        if (rotateSelf) {
            var matrices = context.getContext().getMatrices();
            matrices.pop();
        }
    }
}
