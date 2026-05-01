package itz.silentcore.feature.ui.screen.mainmenu;

import itz.silentcore.SilentCore;
import itz.silentcore.utils.client.IMinecraft;
import itz.silentcore.utils.render.ColorRGBA;
import itz.silentcore.utils.render.RenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static itz.silentcore.utils.render.Fonts.icons;
import static itz.silentcore.utils.render.Fonts.sf_pro;

public class AccountManagerScreen extends Screen implements IMinecraft {

    private final Screen parent;
    private CustomTextFieldWidget usernameField;
    private Button saveButton;
    private Button backButton;
    private Button clearButton;
    private Button randomButton;
    private List<HistoryButton> historyButtons = new ArrayList<>();

    public AccountManagerScreen(Screen parent) {
        super(Text.literal("AccountManager"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = mc.getWindow().getScaledWidth() / 2;
        int centerY = mc.getWindow().getScaledHeight() / 2;

        
        usernameField = new CustomTextFieldWidget(
                centerX - 110,
                centerY - 40,
                220,
                25
        );
        usernameField.setText(SilentCore.getInstance().accountManager.getCustomUsername());

        
        saveButton = new Button(centerX - 160, centerY + 30, 100, 25, "Save", () -> {
            String username = usernameField.getText().trim();
            if (!username.isEmpty()) {
                SilentCore.getInstance().accountManager.setCustomUsername(username);
            }
            mc.setScreen(parent);
            return null;
        });

        clearButton = new Button(centerX - 40, centerY + 30, 100, 25, "Clear", () -> {
            usernameField.setText("");
            return null;
        });

        randomButton = new Button(centerX - 160, centerY + 65, 100, 25, "Random name", () -> {
            String randomUsername = SilentCore.getInstance().accountManager.generateRandomUsername();
            usernameField.setText(randomUsername);
            return null;
        });

        backButton = new Button(centerX + 80, centerY + 30, 100, 25, "Cancel", () -> {
            mc.setScreen(parent);
            return null;
        });

        
        historyButtons.clear();
        int historyStartY = centerY + 80;
        int buttonWidth = 150;
        int buttonHeight = 22;
        int spacingY = 28;

        for (int i = 0; i < SilentCore.getInstance().accountManager.getUsernameHistory().size(); i++) {
            String username = SilentCore.getInstance().accountManager.getUsernameHistory().get(i);
            int x = centerX - 75;
            int y = historyStartY + (i * spacingY);

            historyButtons.add(new HistoryButton(x, y, buttonWidth, buttonHeight, username));
        }
    }

    private class Button {
        int x;
        int y;
        final int width;
        final int height;
        final String text;
        final java.util.function.Supplier<Void> action;

        public Button(int x, int y, int width, int height, String text, java.util.function.Supplier<Void> action) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.text = text;
            this.action = action;
        }

        public void render(RenderContext context, int mouseX, int mouseY) {
            context.drawBlur(x, y, width, height, 5.0f, 8, ColorRGBA.of(255, 255, 255, 255));
            context.drawRect(x, y, width, height, 5.0f, ColorRGBA.of(70, 70, 70, 50));

            context.drawText(
                    text,
                    sf_pro,
                    x + (float) width / 2 - sf_pro.getWidth(text, 10) / 2,
                    y + (float) height / 2 - 5.5f,
                    10.0f,
                    ColorRGBA.of(126, 126, 131)
            );

            context.drawBorder(x, y, width, height, 5.0f, 0.05f, ColorRGBA.of(223, 223, 223, 45), 0.4F, 0.4F);
        }

        public void onClick(int mouseX, int mouseY) {
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
                action.get();
            }
        }
    }

    private void renderPlayerHead(RenderContext renderContext, String username, int x, int y, int size) {
        try {
            
            renderContext.drawRect(x, y, size, size, 2.0f, ColorRGBA.of(100, 100, 100, 200));
            renderContext.drawBorder(x, y, size, size, 2.0f, 0.5f, ColorRGBA.of(164, 143, 255, 100));
        } catch (Exception e) {
            
        }
    }

    private class HistoryButton {
        int x;
        int y;
        final int width;
        final int height;
        final String username;
        final int headSize = 16;

        public HistoryButton(int x, int y, int width, int height, String username) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.username = username;
        }

        public void render(RenderContext context, int mouseX, int mouseY) {
            context.drawBlur(x, y, width + headSize + 8, height, 4.0f, 6, ColorRGBA.of(255, 255, 255, 255));
            context.drawRect(x, y, width + headSize + 8, height, 4.0f, ColorRGBA.of(60, 60, 70, 100));

            
            renderPlayerHead(context, username, x + 3, y + (height - headSize) / 2, headSize);

            
            context.drawText(
                    username,
                    sf_pro,
                    x + headSize + 10,
                    y + (float) height / 2 - 4.5f,
                    9.0f,
                    ColorRGBA.of(164, 143, 255)
            );

            context.drawBorder(x, y, width + headSize + 8, height, 4.0f, 0.05f, ColorRGBA.of(223, 223, 223, 35), 0.4F, 0.4F);
        }

        public void onClick(int mouseX, int mouseY) {
            if (mouseX >= x && mouseX <= x + width + headSize + 8 && mouseY >= y && mouseY <= y + height) {
                usernameField.setText(username);
            }
        }
    }

    private class CustomTextFieldWidget {
        int x;
        int y;
        final int width;
        final int height;
        private String text = "";
        private int cursorPos = 0;
        private boolean focused = false;

        public CustomTextFieldWidget(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public void setText(String text) {
            this.text = text != null ? text.substring(0, Math.min(text.length(), 16)) : "";
            this.cursorPos = this.text.length();
        }

        public String getText() {
            return text;
        }

        public void setFocused(boolean focused) {
            this.focused = focused;
        }

        public boolean isFocused() {
            return focused;
        }

        public void render(RenderContext context, int mouseX, int mouseY) {
            
            context.drawBlur(x, y, width, height, 5.0f, 6, ColorRGBA.of(255, 255, 255, 100));
            context.drawRect(x, y, width, height, 5.0f, ColorRGBA.of(50, 50, 60, 150));
            context.drawBorder(x, y, width, height, 5.0f, 0.5f,
                    focused ? ColorRGBA.of(164, 143, 255, 150) : ColorRGBA.of(164, 143, 255, 80), 0.4F, 0.4F);

            
            context.drawText(
                    text.isEmpty() && !focused ? "Enter username..." : text,
                    sf_pro,
                    x + 10,
                    y + (float) height / 2 - 5.5f,
                    11.0f,
                    text.isEmpty() && !focused ? ColorRGBA.of(126, 126, 131, 100) : ColorRGBA.of(255, 255, 255)
            );

            
            if (focused) {
                float cursorX = x + 10 + sf_pro.getWidth(text.substring(0, Math.min(cursorPos, text.length())), 11);
                context.drawRect(cursorX, y + 5, 1, height - 10, 0, ColorRGBA.of(164, 143, 255));
            }
        }

        public void keyPressed(int keyCode) {
            if (keyCode == 257) { 
                focused = false;
            } else if (keyCode == 259) { 
                if (cursorPos > 0) {
                    text = text.substring(0, cursorPos - 1) + text.substring(cursorPos);
                    cursorPos--;
                }
            } else if (keyCode == 261) { 
                if (cursorPos < text.length()) {
                    text = text.substring(0, cursorPos) + text.substring(cursorPos + 1);
                }
            } else if (keyCode == 263) { 
                if (cursorPos > 0) cursorPos--;
            } else if (keyCode == 262) { 
                if (cursorPos < text.length()) cursorPos++;
            }
        }

        public void charTyped(char chr) {
            if (text.length() < 16 && chr >= 32 && chr < 127) {
                text = text.substring(0, cursorPos) + chr + text.substring(cursorPos);
                cursorPos++;
            }
        }

        public void onClick(int mouseX, int mouseY) {
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
                focused = true;
                cursorPos = text.length();
            } else {
                focused = false;
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        var renderContext = new RenderContext(context);

        // Рисуем шейдерный фон
        try {
            itz.silentcore.utils.render.draw.StarShaderRenderer.render(
                    mc.getWindow().getScaledWidth(), 
                    mc.getWindow().getScaledHeight()
            );
        } catch (Exception e) {
            // Fallback на простой градиентный фон
            renderContext.drawRect(0, 0, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight(), 0,
                    ColorRGBA.of(10, 10, 15, 255),
                    ColorRGBA.of(20, 15, 30, 255),
                    ColorRGBA.of(15, 10, 25, 255),
                    ColorRGBA.of(10, 10, 15, 255));
        }

        int centerX = mc.getWindow().getScaledWidth() / 2;
        int centerY = mc.getWindow().getScaledHeight() / 2;

        
        int containerHeight = 280 + (Math.max(0, historyButtons.size() - 2) * 28);
        int containerWidth = 420;
        int containerX = centerX - containerWidth / 2;
        int containerY = centerY - 120;

        
        renderContext.drawBlur(containerX, containerY, containerWidth, containerHeight, 10.0f, 10, ColorRGBA.of(255, 255, 255, 255));
        renderContext.drawRect(containerX, containerY, containerWidth, containerHeight, 10.0f, ColorRGBA.of(30, 30, 35, 200));
        renderContext.drawBorder(containerX, containerY, containerWidth, containerHeight, 10.0f, 0.5f, ColorRGBA.of(164, 143, 255, 100), 0.4F, 0.4F);

        
        renderContext.drawText(
                "Account Manager",
                sf_pro,
                centerX - sf_pro.getWidth("Account Manager", 22) / 2,
                centerY - 100,
                22.0f,
                ColorRGBA.of(255, 255, 255)
        );

        
        renderContext.drawText(
                "Custom Username:",
                sf_pro,
                centerX - 110,
                centerY - 60,
                11.0f,
                ColorRGBA.of(164, 143, 255)
        );

        
        usernameField.render(renderContext, mouseX, mouseY);


        saveButton.render(renderContext, mouseX, mouseY);
        clearButton.render(renderContext, mouseX, mouseY);
        randomButton.render(renderContext, mouseX, mouseY);
        backButton.render(renderContext, mouseX, mouseY);

        
        if (!historyButtons.isEmpty()) {
            renderContext.drawText(
                    "Recent Usernames:",
                    sf_pro,
                    centerX - 160,
                    centerY + 65,
                    10.0f,
                    ColorRGBA.of(164, 143, 255)
            );

            for (HistoryButton btn : historyButtons) {
                btn.render(renderContext, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        usernameField.onClick((int) mouseX, (int) mouseY);
        saveButton.onClick((int) mouseX, (int) mouseY);
        clearButton.onClick((int) mouseX, (int) mouseY);
        randomButton.onClick((int) mouseX, (int) mouseY);
        backButton.onClick((int) mouseX, (int) mouseY);

        for (HistoryButton btn : historyButtons) {
            btn.onClick((int) mouseX, (int) mouseY);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (usernameField.isFocused()) {
            if (keyCode == 257) { 
                String username = usernameField.getText().trim();
                if (!username.isEmpty()) {
                    SilentCore.getInstance().accountManager.setCustomUsername(username);
                }
                mc.setScreen(parent);
                return true;
            } else if (keyCode == 256) { 
                mc.setScreen(parent);
                return true;
            }
            usernameField.keyPressed(keyCode);
            return true;
        }

        if (keyCode == 256) { 
            mc.setScreen(parent);
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (usernameField.isFocused()) {
            usernameField.charTyped(chr);
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
