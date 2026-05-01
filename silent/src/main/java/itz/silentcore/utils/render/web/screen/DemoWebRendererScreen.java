package itz.silentcore.utils.render.web.screen;

import itz.silentcore.utils.render.web.WebRendererConfig;
import itz.silentcore.utils.render.web.WebRendererInstance;
import itz.silentcore.utils.render.web.WebRendererManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;


public class DemoWebRendererScreen extends Screen {

    private static final int CHROME_HEIGHT = 24;

    private final WebRendererManager rendererManager = WebRendererManager.INSTANCE;
    private final WebRendererConfig config;

    private WebRendererInstance rendererInstance;
    private TextFieldWidget urlField;
    private ButtonWidget goButton;

    private int contentX;
    private int contentY;
    private int contentWidth;
    private int contentHeight;

    public DemoWebRendererScreen(String url) {
        super(Text.translatable("xwebrenderer.screen.demo.title"));
        this.config = WebRendererConfig.builder().url(url).transparent(false).build();
    }

    @Override
    protected void init() {
        rendererManager.initialize();
        rendererInstance = rendererManager.create(config);
        contentX = 8;
        contentY = CHROME_HEIGHT + 6;
        contentWidth = width - 16;
        contentHeight = height - contentY - 8;
        if (rendererInstance != null) {
            rendererInstance.resize(contentWidth, contentHeight);
            rendererInstance.setFocused(true);
        }

        urlField = new TextFieldWidget(textRenderer, 8, 6, width - 8 - 60, CHROME_HEIGHT - 8, Text.translatable("xwebrenderer.screen.demo.url_placeholder"));
        urlField.setText(config.initialUrl());
        addSelectableChild(urlField);
        setInitialFocus(urlField);

        goButton = ButtonWidget.builder(Text.translatable("xwebrenderer.screen.demo.go"), button -> {
            if (rendererInstance != null) {
                rendererInstance.loadUrl(urlField.getText());
            }
        }).dimensions(width - 56, 4, 48, CHROME_HEIGHT - 8).build();
        addDrawableChild(goButton);
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        drawContext.fill(0, 0, width, height, 0xCC101018);
        drawContext.fill(0, 0, width, CHROME_HEIGHT + 4, 0xDD000000);
        super.render(drawContext, mouseX, mouseY, delta);
        if (rendererInstance != null) {
            rendererInstance.render(drawContext, contentX, contentY, contentWidth, contentHeight, delta);
        }
        drawContext.drawBorder(contentX, contentY, contentWidth, contentHeight, 0xFF3F3F3F);
        if (!rendererManager.hasRealEngine()) {
            drawContext.drawTextWithShadow(textRenderer, Text.translatable("xwebrenderer.screen.demo.no_engine"), contentX + 12, contentY + 12, 0xFFD0D0D0);
        }
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (rendererInstance != null) {
            rendererInstance.mouseMove(mouseX - contentX, mouseY - contentY);
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isInsideContent(mouseX, mouseY) && rendererInstance != null) {
            rendererInstance.setFocused(true);
            rendererInstance.mouseButton(mouseX - contentX, mouseY - contentY, button, true, getCurrentModifiers());
            return true;
        }
        if (rendererInstance != null) {
            rendererInstance.setFocused(false);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (rendererInstance != null && isInsideContent(mouseX, mouseY)) {
            rendererInstance.mouseButton(mouseX - contentX, mouseY - contentY, button, false, getCurrentModifiers());
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (rendererInstance != null && isInsideContent(mouseX, mouseY)) {
            rendererInstance.scroll(horizontal, vertical);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (rendererInstance != null && isInsideContent(mouseX, mouseY)) {
            rendererInstance.mouseMove(mouseX - contentX, mouseY - contentY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (rendererInstance != null && !urlField.isFocused()) {
            rendererInstance.keyEvent(keyCode, scanCode, modifiers, true);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (rendererInstance != null && !urlField.isFocused()) {
            rendererInstance.keyEvent(keyCode, scanCode, modifiers, false);
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (rendererInstance != null && !urlField.isFocused()) {
            rendererInstance.charTyped(chr);
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        contentX = 8;
        contentY = CHROME_HEIGHT + 6;
        contentWidth = width - 16;
        contentHeight = height - contentY - 8;
        if (rendererInstance != null) {
            rendererInstance.resize(contentWidth, contentHeight);
        }
        urlField.setWidth(width - 8 - 60);
        urlField.setPosition(8, 6);
        goButton.setPosition(width - 56, 4);
    }

    @Override
    public void removed() {
        super.removed();
        if (rendererInstance != null) {
            rendererInstance.close();
            rendererInstance = null;
        }
        rendererManager.shutdown();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private boolean isInsideContent(double mouseX, double mouseY) {
        return mouseX >= contentX && mouseY >= contentY && mouseX < contentX + contentWidth && mouseY < contentY + contentHeight;
    }

    private int getCurrentModifiers() {
        int modifiers = 0;
        if (hasShiftDown()) {
            modifiers |= 0x1;
        }
        if (hasControlDown()) {
            modifiers |= 0x2;
        }
        if (hasAltDown()) {
            modifiers |= 0x4;
        }
        return modifiers;
    }
}
