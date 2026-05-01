package itz.silentcore.feature.module.impl.render;

import com.google.common.eventbus.Subscribe;
import itz.silentcore.feature.event.impl.AspectRatioEvent;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.feature.module.api.setting.impl.NumberSetting;

@ModuleAnnotation(name = "AspectRatio", category = Category.RENDER, description = "Changes aspect ratio")
public class AspectRatio extends Module {

    private final NumberSetting ratio = new NumberSetting("Ratio", 1.0f, 0.1f, 2.0f, 0.01f);

    @Subscribe
    public void onAspectRatio(AspectRatioEvent event) {
        event.setRatio(ratio.getCurrent());
        event.cancel();
    }
}
