package itz.silentcore.feature.ui.screen.csgui.component.impl.setting;

import itz.silentcore.feature.module.api.setting.impl.NumberSetting;
import itz.silentcore.feature.ui.screen.csgui.component.impl.SettingComponent;
import itz.silentcore.utils.client.ClientUtility;
import itz.silentcore.utils.render.ColorRGBA;
import itz.silentcore.utils.render.Fonts;
import itz.silentcore.utils.render.RenderContext;

public class SliderSettingComponent extends SettingComponent {
    private final NumberSetting setting;
    private boolean dragging = false;
    private float animatedT = -1f;

    public SliderSettingComponent(float x, float y, NumberSetting setting) {
        super(x, y, setting);
        this.setting = setting;
    }

    @Override
    public float getHeight() {
        return 30f;
    }

    @Override
    public void render(RenderContext context) {
        float x = getX();
        float y = getY();
        float w = 101f;

        float labelY = y + 4f;
        String label = setting.getName();
        context.drawText(label, Fonts.sf_pro, x + 6f, labelY, 6f, ColorRGBA.of(233, 233, 233, 220));

        float barPaddingX = 6f;
        float barStartX = x + barPaddingX;
        float barEndX = x + w - barPaddingX;
        float barWidth = barEndX - barStartX;
        float barH = 2f;
        float barY = y + getHeight() / 2f - barH / 2f;

        float min = setting.getMin();
        float max = setting.getMax();
        float cur = Math.max(min, Math.min(setting.getCurrent(), max));
        float t = (max - min) > 0.00001f ? (cur - min) / (max - min) : 0f;

        if (animatedT < 0f) animatedT = t; // initialize on first render
        animatedT += (t - animatedT) * 0.25f;
        float fillW = barWidth * animatedT;

        ColorRGBA base = ColorRGBA.of(255, 255, 255, 200);
        ColorRGBA fill = ClientUtility.getThemePrimaryColorRGBA();
        ColorRGBA textCol = ColorRGBA.of(233, 233, 233, 200);

        // Reduce corner radius of the slider track
        context.drawRect(barStartX, barY, barWidth, barH, 0.6f, base);
        if (fillW > 0.5f) {
            context.drawRect(barStartX, barY, fillW, barH, 0.6f, fill.withAlpha(220));
        }

        float knobSize = 4f;
        float knobX = barStartX + fillW - knobSize / 2f;
        float knobY = barY - knobSize / 2f + barH / 2f;
        // Smaller knob with modest rounding
        context.drawRect(knobX, knobY, knobSize, knobSize, 1.0f, fill);

        String minStr = trimNum(min);
        String maxStr = trimNum(max);
        String curStr = trimNum(cur);

        // Raise the bottom text closer to the slider
        float ty = barY + 3f;
        context.drawText(minStr, Fonts.sf_pro, barStartX, ty, 6f, textCol);
        float maxW = Fonts.sf_pro.getWidth(maxStr, 6f);
        context.drawText(maxStr, Fonts.sf_pro, barEndX - maxW, ty, 6f, textCol);

        float curW = Fonts.sf_pro.getWidth(curStr, 6f);
        context.drawText(curStr, Fonts.sf_pro, x + w / 2f - curW / 2f, ty, 6f, textCol);
    }

    @Override
    public void click(double mouseX, double mouseY, int button) {
        if (button != 0) return;
        if (!isHovered(mouseX, mouseY, 101f, getHeight())) return;
        
        float barY = getY() + getHeight() / 2f - 1f;
        float barHeight = 2f;
        
        // Проверяем клик по слайдеру (расширенная зона клика)
        if (mouseY >= barY - 5f && mouseY <= barY + barHeight + 5f) {
            dragging = true;
            updateValue(mouseX);
        }
    }

    @Override
    public void dragged(double mouseX, double mouseY, double deltaX, double deltaY, int button) {
        if (button == 0 && dragging) {
            updateValue(mouseX);
        }
    }

    @Override
    public void mReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }
    }

    @Override
    public void moved(double mouseX, double mouseY) {
        // Не обновляем значение при простом движении мыши
    }

    private boolean isInside(double mouseX, double mouseY) {
        return isHovered(mouseX, mouseY, 101f, getHeight());
    }

    private void updateValue(double mouseX) {
        float x = getX();
        float w = 101f;
        float barPaddingX = 6f;
        float barWidth = w - barPaddingX * 2f;

        float min = setting.getMin();
        float max = setting.getMax();
        float inc = setting.getIncrement();

        float rel = (float) (mouseX - (x + barPaddingX));
        rel = Math.max(0f, Math.min(rel, barWidth));
        float t = barWidth > 0.00001f ? rel / barWidth : 0f;
        float value = min + t * (max - min);

        if (inc > 0f) {
            value = Math.round((value - min) / inc) * inc + min;
        }
        value = Math.max(min, Math.min(value, max));
        setting.setCurrent(value);
    }

    private String trimNum(float v) {
        if (Math.abs(v - Math.round(v)) < 0.0001f) return Integer.toString(Math.round(v));
        return String.format("%.2f", v);
    }
}
