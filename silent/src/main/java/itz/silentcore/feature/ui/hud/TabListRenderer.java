package itz.silentcore.feature.ui.hud;

import com.google.common.eventbus.Subscribe;
import itz.silentcore.SilentCore;
import itz.silentcore.feature.event.impl.TabListRenderEvent;
import itz.silentcore.utils.render.ColorRGBA;
import itz.silentcore.utils.render.RenderContext;
import net.minecraft.client.MinecraftClient;

public class TabListRenderer {

    public TabListRenderer() {
        try {
            SilentCore.getInstance().eventBus.register(this);
        } catch (Exception e) {
        }
    }

    @Subscribe
    public void onTabListRender(TabListRenderEvent event) {
        RenderContext renderContext = event.getContext();
        int scaledWindowWidth = event.getScaledWindowWidth();

        MinecraftClient mc = MinecraftClient.getInstance();
        int scaledWindowHeight = mc.getWindow().getScaledHeight();

        if (mc.getNetworkHandler() == null) return;

        float radius = 4f;

        int size = mc.getNetworkHandler().getPlayerList().size();
        int columns = 1;
        if (size > 20) columns = 2;
        if (size > 40) columns = 3;

        int width = Math.min(scaledWindowWidth - 16, 160 + columns * 9);
        int height = scaledWindowHeight - 32;

        float bgX = (scaledWindowWidth - width) / 2f;
        float bgY = 10f;

        renderContext.drawBlur(bgX, bgY, width, height, radius, 30, ColorRGBA.of(255, 255, 255, 255));
        renderContext.drawRect(bgX, bgY, width, height, radius, ColorRGBA.of(0, 0, 0, 140));
        renderContext.drawBorder(bgX, bgY, width, height, radius, 0.5f, ColorRGBA.of(80, 80, 80, 120));
    }
}
