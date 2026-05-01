package itz.silentcore.utils.render.web.hud;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import itz.silentcore.utils.render.web.WebRendererConfig;
import itz.silentcore.utils.render.web.WebRendererInstance;
import itz.silentcore.utils.render.web.WebRendererManager;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;


public final class WebHudOverlay implements HudRenderCallback {

    private static final int PANEL_WIDTH = 200;
    private static final int PANEL_HEIGHT = 120;
    private static final int PADDING = 10;

    private static final String HUD_HTML = """
            <!doctype html>
            <html lang="en">
            <head>
              <meta charset="UTF-8"/>
              <style>
                body {
                  margin: 0;
                  font-family: 'Segoe UI', sans-serif;
                  color: #f0f6ff;
                  background: linear-gradient(135deg,#232a3d 0%,#16324f 100%);
                  display: flex;
                  flex-direction: column;
                  align-items: center;
                  justify-content: center;
                  height: 100vh;
                }
                .title {
                  font-size: 18px;
                  margin-bottom: 6px;
                  letter-spacing: 0.04em;
                }
                .status {
                  font-size: 13px;
                  opacity: .85;
                }
              </style>
            </head>
            <body>
              <div class="title">xWebRenderer HUD</div>
              <div class="status">MCEF Chromium Overlay</div>
            </body>
            </html>
            """;

    private static final String HUD_DATA_URL = "data:text/html;base64," +
            Base64.getEncoder().encodeToString(HUD_HTML.getBytes(StandardCharsets.UTF_8));

    private final WebRendererManager manager = WebRendererManager.INSTANCE;

    private WebRendererInstance instance;
    private boolean hudEnabled = true;

    public boolean toggle() {
        hudEnabled = !hudEnabled;
        return hudEnabled;
    }

    public boolean isEnabled() {
        return hudEnabled;
    }

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        if (!hudEnabled) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options.hudHidden || client.getDebugHud().shouldShowDebugHud()) {
            return;
        }
        if (!manager.hasRealEngine()) {
            drawFallback(drawContext);
            return;
        }
        ensureInstance();
        if (instance == null) {
            drawFallback(drawContext);
            return;
        }

        int x = PADDING;
        int y = PADDING;
        int width = PANEL_WIDTH;
        int height = PANEL_HEIGHT;
        drawContext.fill(x - 4, y - 4, x + width + 4, y + height + 4, 0xAA0F1624);
        instance.resize(width, height);
        instance.render(drawContext, x, y, width, height, tickCounter.getLastFrameDuration());
    }

    private void ensureInstance() {
        if (instance != null) {
            return;
        }
        instance = manager.create(WebRendererConfig.builder()
                .url(HUD_DATA_URL)
                .transparent(true)
                .debugGui(false)
                .build());
        if (instance != null) {
            instance.resize(PANEL_WIDTH, PANEL_HEIGHT);
        }
    }

    private void drawFallback(DrawContext drawContext) {
        drawContext.fill(PADDING - 4, PADDING - 4, PADDING + PANEL_WIDTH + 4, PADDING + 32, 0xAA101010);
        drawContext.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                Text.translatable("xwebrenderer.hud.no_engine"), PADDING, PADDING, 0xFFF05A5A);
    }
}
