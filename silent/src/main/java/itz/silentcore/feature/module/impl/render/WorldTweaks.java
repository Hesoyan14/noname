package itz.silentcore.feature.module.impl.render;

import com.google.common.eventbus.Subscribe;
import itz.silentcore.feature.event.impl.FogEvent;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.feature.module.api.setting.impl.MultiBooleanSetting;
import itz.silentcore.feature.module.api.setting.impl.NumberSetting;
import itz.silentcore.utils.client.ClientUtility;

@ModuleAnnotation(name = "WorldTweaks", category = Category.RENDER, description = "Настройки мира")
public class WorldTweaks extends Module {
    
    public final MultiBooleanSetting settings = new MultiBooleanSetting("Настройки мира",
            MultiBooleanSetting.Value.of("Bright"),
            MultiBooleanSetting.Value.of("Time"),
            MultiBooleanSetting.Value.of("Fog"));
    
    public final NumberSetting brightness = new NumberSetting("Яркость", 1.0f, 0.0f, 1.0f, 0.1f, 
            () -> settings.isEnable("Bright"));
    
    public final NumberSetting time = new NumberSetting("Время", 12.0f, 0.0f, 24.0f, 1.0f,
            () -> settings.isEnable("Time"));
    
    public final NumberSetting fogDistance = new NumberSetting("Дистанция тумана", 100.0f, 20.0f, 200.0f, 10.0f,
            () -> settings.isEnable("Fog"));
    
    @Subscribe
    public void onFog(FogEvent event) {
        if (settings.isEnable("Fog")) {
            event.setDistance(fogDistance.getCurrent());
            event.setColor(ClientUtility.getThemePrimaryColor());
            event.cancel();
        }
    }
}
