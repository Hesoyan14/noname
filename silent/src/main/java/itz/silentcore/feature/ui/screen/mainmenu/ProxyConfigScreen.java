package itz.silentcore.feature.ui.screen.mainmenu;

import itz.silentcore.utils.client.IMinecraft;
import itz.silentcore.utils.render.ColorRGBA;
import itz.silentcore.utils.render.RenderContext;
import itz.silentcore.web.proxy.ProxyConfig;
import itz.silentcore.web.proxy.ProxyManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;

import static itz.silentcore.utils.render.Fonts.icons;
import static itz.silentcore.utils.render.Fonts.sf_pro;

public class ProxyConfigScreen extends Screen implements IMinecraft {
    private final Screen parent;
    private final ProxyManager proxyManager = ProxyManager.getInstance();

    private ProxyConfig.ProxyType selectedType = ProxyConfig.ProxyType.NONE;
    private String host = "";
    private String port = "8080";
    private String username = "";
    private String password = "";

    private TextField hostField;
    private TextField portField;
    private TextField usernameField;
    private TextField passwordField;
    private TypeButton[] typeButtons;
    private Button saveButton;
    private Button resetButton;
    private Button backButton;

    private TextField focusedField;
    private int scrollOffset = 0;
    private static final int FOCUSED_HOST = 1;
    private static final int FOCUSED_PORT = 2;
    private static final int FOCUSED_USERNAME = 3;
    private static final int FOCUSED_PASSWORD = 4;
    private int focusedFieldId = 0;

    public ProxyConfigScreen(Screen parent) {
        super(Text.literal("Proxy Config"));
        this.parent = parent;

        ProxyConfig current = proxyManager.getConfig();
        this.selectedType = current.getType();
        this.host = current.getHost();
        this.port = String.valueOf(current.getPort());
        this.username = current.getUsername();
        this.password = current.getPassword();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 60;
        int fieldWidth = 200;
        int fieldHeight = 20;
        int spacing = 35;

        hostField = new TextField(centerX - fieldWidth / 2, startY, fieldWidth, fieldHeight, host);
        portField = new TextField(centerX - fieldWidth / 2, startY + spacing, fieldWidth, fieldHeight, port);
        usernameField = new TextField(centerX - fieldWidth / 2, startY + spacing * 2, fieldWidth, fieldHeight, username);
        passwordField = new TextField(centerX - fieldWidth / 2, startY + spacing * 3, fieldWidth, fieldHeight, password);

        typeButtons = new TypeButton[4];
        ProxyConfig.ProxyType[] types = ProxyConfig.ProxyType.values();
        for (int i = 0; i < types.length; i++) {
            typeButtons[i] = new TypeButton(
                centerX - 120 + (i * 65),
                startY - 45,
                60,
                20,
                types[i],
                types[i] == selectedType
            );
        }

        saveButton = new Button(centerX - 110, this.height - 50, 100, 25, "Save Proxy", () -> {
            try {
                int portNum = Integer.parseInt(portField.text);
                proxyManager.updateConfig(selectedType, host, portNum, username, password);

                try {
                    java.lang.reflect.Method updateMethod = itz.silentcore.web.server.ServerConnection.class.getDeclaredMethod("updateHttpClient");
                    updateMethod.setAccessible(true);
                    updateMethod.invoke(itz.silentcore.web.server.ServerConnection.getInstance());
                } catch (Exception ignored) {
                }

                MinecraftClient.getInstance().setScreen(parent);
            } catch (NumberFormatException e) {
                portField.text = "8080";
            }
            return null;
        });

        resetButton = new Button(centerX + 10, this.height - 50, 100, 25, "Reset", () -> {
            proxyManager.reset();
            selectedType = ProxyConfig.ProxyType.NONE;
            host = "";
            port = "8080";
            username = "";
            password = "";
            hostField.text = "";
            portField.text = "8080";
            usernameField.text = "";
            passwordField.text = "";

            try {
                java.lang.reflect.Method updateMethod = itz.silentcore.web.server.ServerConnection.class.getDeclaredMethod("updateHttpClient");
                updateMethod.setAccessible(true);
                updateMethod.invoke(itz.silentcore.web.server.ServerConnection.getInstance());
            } catch (Exception ignored) {
            }

            return null;
        });

        backButton = new Button(centerX - 55, this.height - 20, 110, 20, "Back", () -> {
            MinecraftClient.getInstance().setScreen(parent);
            return null;
        });
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        RenderContext renderContext = new RenderContext(context);

        // Рисуем шейдерный фон
        try {
            itz.silentcore.utils.render.draw.StarShaderRenderer.render(
                    mc.getWindow().getScaledWidth(), 
                    mc.getWindow().getScaledHeight()
            );
        } catch (Exception e) {
            // Fallback на простой градиентный фон
            renderContext.drawRect(0, 0, this.width, this.height, 0,
                    ColorRGBA.of(10, 10, 15, 255),
                    ColorRGBA.of(20, 15, 30, 255),
                    ColorRGBA.of(15, 10, 25, 255),
                    ColorRGBA.of(10, 10, 15, 255));
        }

        int centerX = this.width / 2;

        renderContext.drawText(
            "Proxy Configuration",
            sf_pro,
            centerX - sf_pro.getWidth("Proxy Configuration", 16) / 2,
            20,
            16.0f,
            ColorRGBA.of(255, 255, 255)
        );

        renderContext.drawText(
            "Proxy Type:",
            sf_pro,
            centerX - 120,
            35,
            10.0f,
            ColorRGBA.of(126, 126, 131)
        );

        for (TypeButton button : typeButtons) {
            button.render(renderContext, mouseX, mouseY);
        }

        renderContext.drawText(
            "Host:",
            sf_pro,
            centerX - 110,
            50,
            10.0f,
            ColorRGBA.of(126, 126, 131)
        );
        hostField.render(renderContext, focusedFieldId == FOCUSED_HOST);

        renderContext.drawText(
            "Port:",
            sf_pro,
            centerX - 110,
            85,
            10.0f,
            ColorRGBA.of(126, 126, 131)
        );
        portField.render(renderContext, focusedFieldId == FOCUSED_PORT);

        renderContext.drawText(
            "Username:",
            sf_pro,
            centerX - 110,
            120,
            10.0f,
            ColorRGBA.of(126, 126, 131)
        );
        usernameField.render(renderContext, focusedFieldId == FOCUSED_USERNAME);

        renderContext.drawText(
            "Password:",
            sf_pro,
            centerX - 110,
            155,
            10.0f,
            ColorRGBA.of(126, 126, 131)
        );
        passwordField.render(renderContext, focusedFieldId == FOCUSED_PASSWORD);

        saveButton.render(renderContext, mouseX, mouseY);
        resetButton.render(renderContext, mouseX, mouseY);
        backButton.render(renderContext, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        for (TypeButton typeButton : typeButtons) {
            if (typeButton.isHovered((int) mouseX, (int) mouseY)) {
                selectedType = typeButton.type;
                for (TypeButton b : typeButtons) {
                    b.selected = b.type == selectedType;
                }
                focusedFieldId = 0;
                return true;
            }
        }

        if (hostField.mouseClicked((int) mouseX, (int) mouseY)) {
            focusedFieldId = FOCUSED_HOST;
            focusedField = hostField;
            return true;
        }
        if (portField.mouseClicked((int) mouseX, (int) mouseY)) {
            focusedFieldId = FOCUSED_PORT;
            focusedField = portField;
            return true;
        }
        if (usernameField.mouseClicked((int) mouseX, (int) mouseY)) {
            focusedFieldId = FOCUSED_USERNAME;
            focusedField = usernameField;
            return true;
        }
        if (passwordField.mouseClicked((int) mouseX, (int) mouseY)) {
            focusedFieldId = FOCUSED_PASSWORD;
            focusedField = passwordField;
            return true;
        }

        focusedFieldId = 0;
        focusedField = null;

        if (saveButton.onClick((int) mouseX, (int) mouseY)) return true;
        if (resetButton.onClick((int) mouseX, (int) mouseY)) return true;
        if (backButton.onClick((int) mouseX, (int) mouseY)) return true;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (focusedFieldId > 0 && focusedField != null) {
            focusedField.type(chr);

            if (focusedFieldId == FOCUSED_HOST) host = hostField.text;
            if (focusedFieldId == FOCUSED_PORT) port = portField.text;
            if (focusedFieldId == FOCUSED_USERNAME) username = usernameField.text;
            if (focusedFieldId == FOCUSED_PASSWORD) password = passwordField.text;

            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            MinecraftClient.getInstance().setScreen(parent);
            return true;
        }

        if (focusedFieldId > 0 && focusedField != null) {
            if (keyCode == 259) {
                focusedField.backspace();

                if (focusedFieldId == FOCUSED_HOST) host = hostField.text;
                if (focusedFieldId == FOCUSED_PORT) port = portField.text;
                if (focusedFieldId == FOCUSED_USERNAME) username = usernameField.text;
                if (focusedFieldId == FOCUSED_PASSWORD) password = passwordField.text;

                return true;
            }

            boolean isCtrlPressed = (modifiers & 1) != 0 || (modifiers & 2) != 0;
            boolean isVKey = keyCode == 86 || scanCode == 47;

            if (isCtrlPressed && isVKey) {
                pasteFromClipboard();
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void pasteFromClipboard() {
        String clipboard = getClipboardText();
        if (clipboard != null && !clipboard.isEmpty()) {
            for (char c : clipboard.toCharArray()) {
                if (focusedField != null) {
                    focusedField.type(c);
                }
            }

            if (focusedFieldId == FOCUSED_HOST) host = hostField.text;
            if (focusedFieldId == FOCUSED_PORT) port = portField.text;
            if (focusedFieldId == FOCUSED_USERNAME) username = usernameField.text;
            if (focusedFieldId == FOCUSED_PASSWORD) password = passwordField.text;
        }
    }

    private String getClipboardText() {
        try {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            java.awt.datatransfer.Clipboard clipboard = toolkit.getSystemClipboard();
            return (String) clipboard.getData(DataFlavor.stringFlavor);
        } catch (Exception e) {
            return null;
        }
    }

    class TextField {
        int x, y, width, height;
        String text;
        int cursorPos;
        private static final int MAX_LENGTH = 64;

        public TextField(int x, int y, int width, int height, String initialText) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.text = initialText != null ? initialText : "";
            this.cursorPos = this.text.length();
        }

        public void render(RenderContext context, boolean focused) {
            ColorRGBA bgColor = focused ? ColorRGBA.of(70, 70, 90, 180) : ColorRGBA.of(50, 50, 50, 150);
            context.drawRect(x, y, width, height, 3.0f, bgColor);

            ColorRGBA borderColor = focused ? ColorRGBA.of(164, 143, 255, 100) : ColorRGBA.of(223, 223, 223, 45);
            context.drawBorder(x, y, width, height, 3.0f, 0.1f, borderColor, 0.4f, 0.4f);

            String displayText = text.length() > 20 ? text.substring(text.length() - 20) : text;
            context.drawText(
                displayText,
                sf_pro,
                x + 5,
                y + (float) height / 2 - 4,
                9.0f,
                ColorRGBA.of(255, 255, 255)
            );

            if (focused) {
                context.drawText(
                    "|",
                    sf_pro,
                    x + 5 + sf_pro.getWidth(displayText, 9.0f),
                    y + (float) height / 2 - 4,
                    9.0f,
                    ColorRGBA.of(164, 143, 255)
                );
            }
        }

        public boolean mouseClicked(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        public void type(char chr) {
            if (text.length() < MAX_LENGTH && chr >= 32 && chr < 127) {
                text += chr;
                cursorPos++;
            }
        }

        public void backspace() {
            if (text.length() > 0) {
                text = text.substring(0, text.length() - 1);
                cursorPos--;
            }
        }
    }

    class TypeButton {
        int x, y, width, height;
        ProxyConfig.ProxyType type;
        boolean selected;

        public TypeButton(int x, int y, int width, int height, ProxyConfig.ProxyType type, boolean selected) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.type = type;
            this.selected = selected;
        }

        public void render(RenderContext context, int mouseX, int mouseY) {
            ColorRGBA bgColor = selected ? ColorRGBA.of(100, 100, 150, 100) : ColorRGBA.of(50, 50, 50, 80);
            context.drawRect(x, y, width, height, 3.0f, bgColor);
            context.drawBorder(x, y, width, height, 3.0f, 0.05f, ColorRGBA.of(223, 223, 223, 45), 0.4f, 0.4f);

            String text = type.getDisplayName();
            context.drawText(
                text,
                sf_pro,
                x + width / 2 - sf_pro.getWidth(text, 8) / 2,
                y + height / 2 - 4,
                8.0f,
                selected ? ColorRGBA.of(255, 255, 255) : ColorRGBA.of(126, 126, 131)
            );
        }

        public boolean isHovered(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }

    class Button {
        int x, y, width, height;
        String text;
        java.util.function.Supplier<Void> action;

        public Button(int x, int y, int width, int height, String text, java.util.function.Supplier<Void> action) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.text = text;
            this.action = action;
        }

        public void render(RenderContext context, int mouseX, int mouseY) {
            context.drawBlur(x, y, width, height, 4.0f, 8, ColorRGBA.of(255, 255, 255, 255));
            context.drawRect(x, y, width, height, 4.0f, ColorRGBA.of(70, 70, 70, 80));

            context.drawText(
                text,
                sf_pro,
                x + width / 2 - sf_pro.getWidth(text, 9) / 2,
                y + height / 2 - 4,
                9.0f,
                ColorRGBA.of(126, 126, 131)
            );

            context.drawBorder(x, y, width, height, 4.0f, 0.05f, ColorRGBA.of(223, 223, 223, 45), 0.4f, 0.4f);
        }

        public boolean onClick(int mouseX, int mouseY) {
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
                action.get();
                return true;
            }
            return false;
        }
    }
}