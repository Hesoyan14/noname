package itz.silentcore.feature.module.impl.render;

import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.feature.module.api.setting.impl.BooleanSetting;

@ModuleAnnotation(name = "BetterMinecraft", category = Category.RENDER, description = "Better Minecraft UI improvements")
public class BetterMinecraft extends Module {
    
    private final BooleanSetting betterButtons = new BooleanSetting("Better Buttons", true);
    private final BooleanSetting tabVanish = new BooleanSetting("Tab Vanish", true);
    
    public boolean isBetterButtonsEnabled() {
        return isEnabled() && betterButtons.isEnabled();
    }
    
    public boolean isTabVanishEnabled() {
        return isEnabled() && tabVanish.isEnabled();
    }
}
