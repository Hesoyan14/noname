package itz.silentcore.feature.module.impl.render;

import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.feature.module.api.setting.impl.MultiBooleanSetting;

@ModuleAnnotation(name = "NoRender", category = Category.RENDER, description = "Disable rendering of certain elements")
public class NoRender extends Module {
    
    public final MultiBooleanSetting elements = new MultiBooleanSetting("Элементы",
            MultiBooleanSetting.Value.of("Fire"),
            MultiBooleanSetting.Value.of("Bad Effects"),
            MultiBooleanSetting.Value.of("Block Overlay"),
            MultiBooleanSetting.Value.of("Darkness"),
            MultiBooleanSetting.Value.of("Damage"));
}
