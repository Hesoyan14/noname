package itz.silentcore.feature.ui.screen.csgui.component.impl.setting;

import itz.silentcore.feature.module.api.setting.impl.MultiBooleanSetting;
import itz.silentcore.feature.ui.screen.csgui.CsGui;
import itz.silentcore.feature.ui.screen.csgui.component.impl.SettingComponent;
import itz.silentcore.utils.render.ColorRGBA;
import itz.silentcore.utils.render.Fonts;
import itz.silentcore.utils.render.RenderContext;

public class MultiBooleanSettingComponent extends SettingComponent {
    private final MultiBooleanSetting setting;
    private boolean expanded = false;

    public MultiBooleanSettingComponent(float x, float y, MultiBooleanSetting setting) {
        super(x, y, setting);
        this.setting = setting;
    }

    @Override
    public float getHeight() {
        if (!expanded) return 20f;
        return 20f + setting.getBooleanSettings().size() * 18f;
    }

    @Override
    public void render(RenderContext context) {
        float x = getX();
        float y = getY();
        
        // Заголовок
        String label = setting.getName();
        context.drawText(label, Fonts.sf_pro, x + 6f, y + 4f, 6f, 
                ColorRGBA.of(233, 233, 233, (int) (220 * CsGui.alpha.getValue())));
        
        // Стрелка для раскрытия
        String arrow = expanded ? "▼" : "▶";
        float arrowX = x + 101f - 12f;
        context.drawText(arrow, Fonts.sf_pro, arrowX, y + 4f, 6f,
                ColorRGBA.of(233, 233, 233, (int) (180 * CsGui.alpha.getValue())));
        
        if (expanded) {
            float optionY = y + 20f;
            for (MultiBooleanSetting.Value value : setting.getBooleanSettings()) {
                boolean enabled = value.isEnabled();
                
                // Чекбокс
                float checkX = x + 10f;
                float checkY = optionY + 2f;
                float checkSize = 8f;
                
                ColorRGBA boxColor = ColorRGBA.of(255, 255, 255, (int) (100 * CsGui.alpha.getValue()));
                ColorRGBA primaryColor = itz.silentcore.utils.client.ClientUtility.getThemePrimaryColorRGBA();
                ColorRGBA fillColor = ColorRGBA.of(primaryColor.getR(), primaryColor.getG(), primaryColor.getB(), (int) (255 * CsGui.alpha.getValue()));
                
                context.drawRect(checkX, checkY, checkSize, checkSize, 2f, boxColor);
                if (enabled) {
                    context.drawRect(checkX + 2f, checkY + 2f, checkSize - 4f, checkSize - 4f, 1f, fillColor);
                }
                
                // Текст опции
                context.drawText(value.getName(), Fonts.sf_pro, checkX + checkSize + 4f, optionY + 2f, 6f,
                        ColorRGBA.of(233, 233, 233, (int) (200 * CsGui.alpha.getValue())));
                
                optionY += 18f;
            }
        }
    }

    @Override
    public void click(double mouseX, double mouseY, int button) {
        if (button != 0) return;
        
        float x = getX();
        float y = getY();
        
        // Клик по заголовку - раскрыть/свернуть
        if (mouseY >= y && mouseY <= y + 20f) {
            expanded = !expanded;
            return;
        }
        
        // Клик по опциям
        if (expanded) {
            float optionY = y + 20f;
            for (MultiBooleanSetting.Value value : setting.getBooleanSettings()) {
                if (mouseY >= optionY && mouseY <= optionY + 18f) {
                    value.toggle();
                    return;
                }
                optionY += 18f;
            }
        }
    }
}
