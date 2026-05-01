package itz.silentcore.feature.ui.screen.csgui.component.impl.setting;

import itz.silentcore.feature.module.api.setting.Setting;
import itz.silentcore.feature.ui.screen.csgui.component.impl.SettingComponent;

public class TestSetting extends SettingComponent {
    public TestSetting(float x, float y, Setting parent) {
        super(x, y, parent);
    }

    @Override
    public float getHeight() {
        return 20;
    }
}
