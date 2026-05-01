package itz.silentcore.feature.ui.screen.csgui.component.impl.setting;

import itz.silentcore.feature.module.api.setting.impl.BooleanSetting;
import itz.silentcore.feature.ui.screen.csgui.CsGui;
import itz.silentcore.feature.ui.screen.csgui.component.impl.SettingComponent;
import itz.silentcore.utils.animation.Animation;
import itz.silentcore.utils.animation.Easing;
import itz.silentcore.utils.render.ColorRGBA;
import itz.silentcore.utils.render.Fonts;
import itz.silentcore.utils.render.RenderContext;

public class BooleanSettingComponent extends SettingComponent {
    private final BooleanSetting setting;
    private final Animation toggleAnim = new Animation(200, Easing.EXPO_OUT);

    public BooleanSettingComponent(float x, float y, BooleanSetting setting) {
        super(x, y, setting);
        this.setting = setting;
    }

    @Override
    public void render(RenderContext context) {
        toggleAnim.animate(setting.isEnabled() ? 1 : 0);
        toggleAnim.update();

        float alpha = (float) CsGui.alpha.getValue();
        
        // Draw setting name
        context.drawText(setting.getName(), Fonts.sf_pro, getX() + 5f, getY() + 3f, 6f, 
            ColorRGBA.of(255, 255, 255, (int)(255 * alpha * 0.8f)));
        
        // Draw toggle switch
        float toggleX = getX() + 101f - 25f;
        float toggleY = getY() + 2f;
        float toggleW = 20f;
        float toggleH = 10f;
        
        // Background
        ColorRGBA bgColor = setting.isEnabled() ? 
            ColorRGBA.of(100, 200, 100, (int)(255 * alpha * 0.8f)) : 
            ColorRGBA.of(60, 60, 60, (int)(255 * alpha * 0.8f));
        context.drawRect(toggleX, toggleY, toggleW, toggleH, 5f, bgColor);
        
        // Circle (using small rect as circle)
        float circleSize = 6f;
        float circleX = toggleX + 2f + (toggleW - circleSize - 4f) * (float)toggleAnim.getValue();
        float circleY = toggleY + 2f;
        context.drawRect(circleX, circleY, circleSize, circleSize, 3f, 
            ColorRGBA.of(255, 255, 255, (int)(255 * alpha)));
    }

    @Override
    public void click(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY, 101, getHeight())) {
            setting.setEnabled(!setting.isEnabled());
        }
    }

    @Override
    public float getHeight() {
        return 14f;
    }
}
