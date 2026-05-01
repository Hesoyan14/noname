package itz.silentcore.feature.ui.screen.csgui.component.impl.setting;

import itz.silentcore.feature.module.api.setting.impl.ModeSetting;
import itz.silentcore.feature.ui.screen.csgui.CsGui;
import itz.silentcore.feature.ui.screen.csgui.component.impl.SettingComponent;
import itz.silentcore.utils.animation.Animation;
import itz.silentcore.utils.animation.Easing;
import itz.silentcore.utils.render.ColorRGBA;
import itz.silentcore.utils.render.Fonts;
import itz.silentcore.utils.render.RenderContext;

public class ModeSettingComponent extends SettingComponent {
    private final ModeSetting setting;
    private boolean expanded = false;
    private final Animation expandAnim = new Animation(200, Easing.EXPO_OUT);

    public ModeSettingComponent(float x, float y, ModeSetting setting) {
        super(x, y, setting);
        this.setting = setting;
    }

    @Override
    public void render(RenderContext context) {
        expandAnim.animate(expanded ? 1 : 0);
        expandAnim.update();

        float alpha = (float) CsGui.alpha.getValue();
        
        // Draw setting name
        context.drawText(setting.getName(), Fonts.sf_pro, getX() + 5f, getY() + 3f, 6f, 
            ColorRGBA.of(255, 255, 255, (int)(255 * alpha * 0.8f)));
        
        // Draw current value
        String currentValue = setting.get();
        float valueWidth = Fonts.sf_pro.getWidth(currentValue, 5.5f);
        context.drawText(currentValue, Fonts.sf_pro, getX() + 101f - 5f - valueWidth, getY() + 3.5f, 5.5f, 
            ColorRGBA.of(200, 200, 200, (int)(255 * alpha * 0.7f)));
        
        // Draw expanded options
        if (expandAnim.getValue() > 0.01) {
            float optionY = getY() + 14f;
            for (ModeSetting.Value value : setting.getValues()) {
                float optionAlpha = (float)(alpha * expandAnim.getValue());
                boolean isSelected = value.isSelected();
                
                ColorRGBA textColor = isSelected ? 
                    ColorRGBA.of(100, 200, 100, (int)(255 * optionAlpha)) : 
                    ColorRGBA.of(180, 180, 180, (int)(255 * optionAlpha * 0.7f));
                
                context.drawText("  " + value.name(), Fonts.sf_pro, getX() + 8f, optionY + 2f, 5.5f, textColor);
                optionY += 12f * (float)expandAnim.getValue();
            }
        }
    }

    @Override
    public void click(double mouseX, double mouseY, int button) {
        if (button == 1 && isHovered(mouseX, mouseY, 101, 14)) {
            expanded = !expanded;
            return;
        }
        
        if (expanded && button == 0) {
            float optionY = getY() + 14f;
            for (ModeSetting.Value value : setting.getValues()) {
                if (mouseY >= optionY && mouseY <= optionY + 12f) {
                    value.select();
                    expanded = false;
                    return;
                }
                optionY += 12f;
            }
        }
    }

    @Override
    public float getHeight() {
        float baseHeight = 14f;
        if (expandAnim.getValue() > 0.01) {
            baseHeight += setting.getValues().size() * 12f * (float)expandAnim.getValue();
        }
        return baseHeight;
    }
}
