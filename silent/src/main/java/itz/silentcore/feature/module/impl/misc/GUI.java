package itz.silentcore.feature.module.impl.misc;

import com.google.common.eventbus.Subscribe;
import itz.silentcore.feature.event.impl.Render2DEvent;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.utils.animation.Animation;
import itz.silentcore.utils.animation.Easing;
import org.lwjgl.glfw.GLFW;

@ModuleAnnotation(name = "GUI", category = Category.MISC, description = "GUI")
public class GUI extends Module {
    private final Animation scaleAnimation = new Animation(250, Easing.BAKEK_SIZE);



    @Override
    public void onEnable() {
       // ClientUtility.sendMessage("enable");
        scaleAnimation.setTarget(1.0f);
    }

    @Override
    public void onDisable() {
      //  ClientUtility.sendMessage("disable");
        scaleAnimation.setTarget(0.0f);
    }

    @Subscribe
    public void render(Render2DEvent event) {
        float scale = scaleAnimation.getValue();

        if (!isEnabled() && scale <= 0.01f) return;

        float screenWidth = mc.getWindow().getScaledWidth();
        float width = 155 * scale;
        float height = 24 * scale;
        float x = (screenWidth - width) / 2 - 300;
        float y = 20;
    }
}
