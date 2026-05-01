package itz.silentcore.feature.ui.hud;

import itz.silentcore.feature.event.impl.MouseClickEvent;
import itz.silentcore.feature.event.impl.Render2DEvent;
import itz.silentcore.feature.ui.hud.drag.DragComponent;
import itz.silentcore.utils.animation.Animation;
import itz.silentcore.utils.animation.Easing;
import itz.silentcore.utils.client.Constants;
import itz.silentcore.utils.render.ColorRGBA;
import itz.silentcore.utils.client.ClientUtility;
import itz.silentcore.web.server.WeatherParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.Identifier;
import ru.kotopushka.compiler.sdk.classes.Profile;
import com.google.common.eventbus.Subscribe;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.Locale;

import static itz.silentcore.utils.client.IMinecraft.mc;
import static itz.silentcore.utils.render.Fonts.sf_pro;
import static itz.silentcore.utils.render.Fonts.icons;

public class WatermarkRenderer extends DragComponent {

    private float serverIpClickX = 0f;
    private float serverIpClickY = 0f;
    private float serverIpClickWidth = 0f;
    private float serverIpClickHeight = 0f;
    private String lastServerIp = "";

    private final String[] textCycle = {"Silent-Core {/}", "https://t.me/SilenCoreClient", "silent.lol", "silent-core.ru", "build: " + Constants.BUILD};
    private int currentTextIndex = 0;
    private Animation textAnimation;
    private long lastSwitchTime = 0;
    private float smoothedAnimatedTextWidth = 0f;

    public WatermarkRenderer() {
        super("watermark");
        setDraggable(false);
        setAllowDragX(false);
        setAllowDragY(false);
        setX(10);
        setY(10);
        lastSwitchTime = System.currentTimeMillis();
        textAnimation = new Animation(2500, Easing.SINE_IN_OUT);
        textAnimation.animate(1);
        try {
            itz.silentcore.SilentCore.getInstance().eventBus.register(this);
        } catch (Exception e) {
        }
    }

    @Subscribe
    public void onMouseClick(MouseClickEvent event) {
        if (event.getButton() != 0) {
            return;
        }

        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();

        boolean xMatch = mouseX >= serverIpClickX && mouseX <= serverIpClickX + serverIpClickWidth;
        boolean yMatch = mouseY >= serverIpClickY && mouseY <= serverIpClickY + serverIpClickHeight;

        if (xMatch && yMatch && !lastServerIp.isEmpty() && mc.currentScreen instanceof ChatScreen) {
            copyServerIpToClipboard(lastServerIp);
        }
    }

    private void copyServerIpToClipboard(String ip) {
        try {
            boolean success = tryAwtClipboard(ip);

            if (!success) {
                String osName = System.getProperty("os.name").toLowerCase();
                if (osName.contains("win")) {
                    success = tryWindowsClipboard(ip);
                } else if (osName.contains("mac")) {
                    success = tryMacClipboard(ip);
                } else if (osName.contains("nux") || osName.contains("nix")) {
                    success = tryLinuxClipboard(ip);
                }
            }

            if (success) {
                NotificationRenderer.pushWithIcon("Айпи сервера скопирован", "P", ClientUtility.getThemePrimaryColorRGBA());
            }
        } catch (Exception e) {
            NotificationRenderer.push("Ошибка копирования IP");
        }
    }

    private boolean tryAwtClipboard(String text) {
        try {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            java.awt.datatransfer.Clipboard clipboard = toolkit.getSystemClipboard();
            if (clipboard != null) {
                clipboard.setContents(new StringSelection(text), null);
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    private boolean tryWindowsClipboard(String text) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"cmd", "/c", "echo " + text + "|clip"});
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean tryMacClipboard(String text) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"pbcopy"});
            process.getOutputStream().write(text.getBytes());
            process.getOutputStream().close();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean tryLinuxClipboard(String text) {
        try {
            String[][] commands = {
                {"xclip", "-selection", "clipboard"},
                {"xsel", "-ib"},
                {"wl-copy"}
            };

            for (String[] command : commands) {
                try {
                    Process process = Runtime.getRuntime().exec(command);
                    process.getOutputStream().write(text.getBytes());
                    process.getOutputStream().close();
                    if (process.waitFor() == 0) {
                        return true;
                    }
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Override
    public void render(Render2DEvent event) {
        // Hide watermark when F3 debug screen is open
        if (MinecraftClient.getInstance().getDebugHud().shouldShowDebugHud()) {
            return;
        }

        var context = event.getContext();

        float x = 10f;
        float y = 6f;
        float paddingX = 6f;
        float paddingY = 4f;
        float radius = 5.0f;
        float textSize = 8.0f;
        float iconSize = textSize + 2.0f;
        float gap = 6.0f;

        String nick = String.valueOf(Profile.getUsername());
        String serverIp = String.valueOf(ClientUtility.getServerIP());
        String fpsLabel = ClientUtility.getFPS() + " FPS";
        String tpsValue = String.format(Locale.US, "%.0f", (double) ClientUtility.getTPS());
        String tpsText = tpsValue + " TPS";
        String weatherText = WeatherParser.weather() + "°";

        Identifier avatarIdPrimary = Identifier.of("silentcore", "textures/bludnov.png");
        Identifier avatarIdFallback = Identifier.of("silentcore", "textures/icon.png");
        var textureManager = MinecraftClient.getInstance().getTextureManager();
        var avatarTexPrimary = textureManager.getTexture(avatarIdPrimary);
        int avatarGlId = avatarTexPrimary != null ? avatarTexPrimary.getGlId() : 0;
        if (avatarGlId == 0) {
            var avatarTexFallback = textureManager.getTexture(avatarIdFallback);
            avatarGlId = avatarTexFallback != null ? avatarTexFallback.getGlId() : 0;
        }

        float iWidth = icons.getWidth("i", iconSize);

        float maxAnimatedTextWidth = 0;
        for (String text : textCycle) {
            maxAnimatedTextWidth = Math.max(maxAnimatedTextWidth, sf_pro.getWidth(text, textSize));
        }

        textAnimation.update();
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSwitchTime >= 5000) {
            lastSwitchTime = currentTime;
            currentTextIndex = (currentTextIndex + 1) % textCycle.length;
            textAnimation = new Animation(5000, Easing.LINEAR);
            textAnimation.animate(1);
        }

        String fullText = textCycle[currentTextIndex];
        float animValue = textAnimation.getValue();

        int totalChars = fullText.length();
        int displayChars;

        float printEnd = 0.30f;
        float holdStart = 0.30f;
        float eraseStart = 0.70f;

        if (animValue < printEnd) {
            displayChars = (int) (totalChars * (animValue / printEnd));
        } else if (animValue < eraseStart) {
            displayChars = totalChars;
        } else {
            float eraseProgress = (animValue - eraseStart) / (1.0f - eraseStart);
            displayChars = (int) (totalChars * (1.0f - eraseProgress));
        }
        displayChars = Math.max(0, Math.min(totalChars, displayChars));

        String displayText = fullText.substring(0, displayChars);
        float currentAnimatedTextWidth = sf_pro.getWidth(displayText, textSize);

        float smoothingAlpha = 0.15f;
        smoothedAnimatedTextWidth += (currentAnimatedTextWidth - smoothedAnimatedTextWidth) * smoothingAlpha;

        float nickWidth = sf_pro.getWidth(nick, textSize);
        float nIconWidth = icons.getWidth("N", iconSize);
        float ipWidth = sf_pro.getWidth(serverIp, textSize);
        float lIconWidth = icons.getWidth("L", iconSize);
        float fpsWidth = sf_pro.getWidth(fpsLabel, textSize);
        float tIconWidth = icons.getWidth("t", iconSize);
        float tpsTextWidth = sf_pro.getWidth(tpsText, textSize);
        float qIconWidth = icons.getWidth("q", iconSize);
        float weatherWidth = sf_pro.getWidth(weatherText, textSize);

        float sfHeight = sf_pro.getMetrics().lineHeight() * textSize;
        float iconHeight = icons.getMetrics().lineHeight() * iconSize;
        float textHeight = Math.max(sfHeight, iconHeight);
        float avatarSize = Math.max(12.0f, textHeight * 0.95f);

        float width = (
                iWidth + gap +
                smoothedAnimatedTextWidth + gap +
                avatarSize + gap +
                nickWidth + gap +
                nIconWidth + gap +
                ipWidth + gap +
                lIconWidth + gap +
                fpsWidth + gap +
                tIconWidth + gap + tpsTextWidth + gap +
                qIconWidth + 2.0f + weatherWidth
        ) + paddingX * 2f;
        float height = textHeight + paddingY * 2f;

        context.drawBlur(x, y, width, height, radius, 30, ColorRGBA.of(255, 255, 255, 255));
        context.drawRect(x, y, width, height, radius, ColorRGBA.of(0, 0, 0, 166));

        float cursorX = x + paddingX;
        float centerY = y + height / 2f;
        float sfY = centerY - sfHeight / 2f;
        float iconY = centerY - iconHeight / 2f;

        ColorRGBA themeColor = ClientUtility.getThemePrimaryColorRGBA();
        context.drawText("i", icons, cursorX, iconY, iconSize, themeColor);
        cursorX += iWidth + gap;

        ColorRGBA textColor = ColorRGBA.of(255, 255, 255);
        context.drawText(displayText, sf_pro, cursorX, sfY, textSize, textColor);
        cursorX += smoothedAnimatedTextWidth + gap;

        float avatarY = centerY - avatarSize / 2f;
        if (avatarGlId != 0) {
            context.drawTexture(
                    cursorX, avatarY,
                    avatarSize, avatarSize,
                    avatarSize / 2f,
                    ColorRGBA.of(255, 255, 255),
                    0f, 0f, 1f, 1f,
                    avatarGlId
            );
        }
        cursorX += avatarSize + gap;

        context.drawText(nick, sf_pro, cursorX, sfY, textSize, ColorRGBA.of(255, 255, 255));
        cursorX += nickWidth + gap;

        context.drawText("N", icons, cursorX, iconY, iconSize, themeColor);
        cursorX += nIconWidth + gap;

        serverIpClickX = cursorX;
        serverIpClickY = y;
        serverIpClickWidth = ipWidth;
        serverIpClickHeight = height;
        lastServerIp = serverIp;

        context.drawText(serverIp, sf_pro, cursorX, sfY, textSize, ColorRGBA.of(255, 255, 255));
        cursorX += ipWidth + gap;

        context.drawText("L", icons, cursorX, iconY, iconSize, themeColor);
        cursorX += lIconWidth + gap;

        context.drawText(fpsLabel, sf_pro, cursorX, sfY, textSize, ColorRGBA.of(255, 255, 255));
        cursorX += fpsWidth + gap;

        context.drawText("t", icons, cursorX, iconY, iconSize, themeColor);
        cursorX += tIconWidth + gap;
        context.drawText(tpsText, sf_pro, cursorX, sfY, textSize, ColorRGBA.of(255, 255, 255));
        cursorX += tpsTextWidth + gap;

        context.drawText("q", icons, cursorX, iconY, iconSize, themeColor);
        cursorX += qIconWidth + 2.0f;
        context.drawText(weatherText, sf_pro, cursorX, sfY, textSize, ColorRGBA.of(255, 255, 255));

        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
    }
}
