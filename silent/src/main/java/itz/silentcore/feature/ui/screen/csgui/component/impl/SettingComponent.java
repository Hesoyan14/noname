package itz.silentcore.feature.ui.screen.csgui.component.impl;

import itz.silentcore.feature.module.api.setting.Setting;
import itz.silentcore.feature.ui.screen.csgui.component.Component;

public abstract class SettingComponent extends Component {
    public Setting parent;

    public SettingComponent(float x, float y, Setting parent) {
        super(x, y);
        this.parent = parent;
    }

    public abstract float getHeight();
    
    public Setting getSetting() {
        return parent;
    }
}