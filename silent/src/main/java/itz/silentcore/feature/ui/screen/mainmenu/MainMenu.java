package itz.silentcore.feature.ui.screen.mainmenu;

import itz.silentcore.utils.animation.Animation;
import itz.silentcore.utils.animation.Easing;
import itz.silentcore.utils.client.IMinecraft;
import itz.silentcore.utils.render.ColorRGBA;
import itz.silentcore.utils.render.RenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;

import java.util.function.Supplier;

import static itz.silentcore.utils.render.Fonts.icons;
import static itz.silentcore.utils.render.Fonts.sf_pro;

public class MainMenu extends Screen implements IMinecraft {
    Button[] buttons;
    private Animation textAnimation;

    private long lastSwitchTime = 0;
    private String[] textCycle;
    private int currentTextIndex = 0;

    public MainMenu() {
        super(Text.literal("MainMenu"));
        textCycle = new String[]{"Welcome back to SilentCore"};
        textAnimation = new Animation(2500, Easing.SINE_IN_OUT);
        textAnimation.animate(1);
        lastSwitchTime = System.currentTimeMillis();
    }

    @Override
    protected void init() {
        updateButtons();
    }

    @Override
    public void tick() {
        updateButtons();
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

        textAnimation.update();
        String fullText = textCycle[currentTextIndex];

        // Разделяем текст на "Welcome back to " и "SilentCore"
        String prefix = "Welcome back to ";
        String suffix = "SilentCore";

        float textY = (float) mc.getWindow().getScaledHeight() / 2 - 76;
        float prefixWidth = sf_pro.getWidth(prefix, 12);
        float totalWidth = sf_pro.getWidth(fullText, 12);
        float startX = (float) mc.getWindow().getScaledWidth() / 2 - totalWidth / 2;

        // Рисуем "Welcome back to " белым
        renderContext.drawText(
                prefix,
                sf_pro,
                startX,
                textY,
                12.0f,
                ColorRGBA.of(255, 255, 255)
        );

        // Рисуем "SilentCore" фиолетовым
        renderContext.drawText(
                suffix,
                sf_pro,
                startX + prefixWidth,
                textY,
                12.0f,
                itz.silentcore.utils.client.ClientUtility.getThemePrimaryColorRGBA()
        );

        for (Button button : buttons) {
            button.render(renderContext, mouseX, mouseY);
        }
    }

    class Button {
        int x;
        int y;
        final int width;
        final int height;
        final String text;
        final Supplier<Void> action;

        public Button(int x, int y, int width, int height, String text, Supplier<Void> action) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.text = text;
            this.action = action;
        }

        public void render(RenderContext context, int mouseX, int mouseY) {
            boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
            boolean isExitButton = text.equals("Exit");

            context.drawBlur(x, y, width, height, 5.0f, 8, ColorRGBA.of(255, 255, 255, 255), 0.8f);

            // Красный фон для Exit при наведении
            if (isExitButton && isHovered) {
                context.drawRect(x, y, width, height, 5.0f, ColorRGBA.of(180, 50, 50, 80));
            } else {
                context.drawRect(x, y, width, height, 5.0f, ColorRGBA.of(70, 70, 70, 30));
            }

            // Красный текст для Exit при наведении
            ColorRGBA textColor = (isExitButton && isHovered)
                    ? ColorRGBA.of(255, 100, 100)
                    : ColorRGBA.of(126, 126, 131);

            context.drawText(
                    text,
                    sf_pro,
                    x + 7,
                    y + (float) height / 2 - 5.5f,
                    10.0f,
                    textColor
            );

            context.drawText(
                    getIconSymbol(text),
                    icons,
                    x + width - 17.5f,
                    y + (float) height / 2 - 4.5f,
                    10.0f,
                    textColor
            );

            context.drawBorder(x, y, width, height, 5.0f, 0.05f,
                    ColorRGBA.of(223, 223, 223, 30), 0.4F, 0.4F);
        }

        public void onClick(int mouseX, int mouseY) {
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
                action.get();
            }
        }
    }

    String getIconSymbol(String text) {
        return switch (text) {
            case "Singleplayer" -> "Q";
            case "Multiplayer" -> "G";
            case "AccountManager" -> "H";
            case "Proxy Config" -> "B";
            case "Exit" -> "F";
            case "Settings" -> "E";
            default -> "a";
        };
    }

    void updateButtons() {
        var width = 210;
        var height = 29;
        var centerX = mc.getWindow().getScaledWidth() / 2 - width / 2;
        var spacing = 33;
        var startY = mc.getWindow().getScaledHeight() / 2 - 55;

        buttons = new Button[]{
                new Button(centerX, startY, width, height, "Singleplayer", () -> {
                    MinecraftClient.getInstance().setScreen(new SelectWorldScreen(this));
                    return null;
                }),
                new Button(centerX, startY + spacing, width, height, "Multiplayer", () -> {
                    MinecraftClient.getInstance().setScreen(new MultiplayerScreen(this));
                    return null;
                }),
                new Button(centerX, startY + spacing * 2, width, height, "AccountManager", () -> {
                    MinecraftClient.getInstance().setScreen(new AccountManagerScreen(this));
                    return null;
                }),
                new Button(centerX, startY + spacing * 3, width, height, "Proxy Config", () -> {
                    MinecraftClient.getInstance().setScreen(new ProxyConfigScreen(this));
                    return null;
                }),
                new Button(centerX + (width / 2) + 2, startY + spacing * 4, width / 2 - 2, height, "Settings", () -> {
                    MinecraftClient.getInstance().setScreen(new OptionsScreen(this, mc.options));
                    return null;
                }),
                new Button(centerX, startY + spacing * 4, width / 2 - 2, height, "Exit", () -> {
                    MinecraftClient.getInstance().scheduleStop();
                    return null;
                })
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        for (Button buttons : buttons) {
            buttons.onClick((int) mouseX, (int) mouseY);
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
