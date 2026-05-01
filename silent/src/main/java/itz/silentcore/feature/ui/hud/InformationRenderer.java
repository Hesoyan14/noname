package itz.silentcore.feature.ui.hud;

import itz.silentcore.SilentCore;
import itz.silentcore.feature.event.impl.MouseClickEvent;
import itz.silentcore.feature.event.impl.Render2DEvent;
import itz.silentcore.feature.ui.hud.drag.DragComponent;
import itz.silentcore.utils.player.MoveUtil;
import itz.silentcore.utils.render.ColorRGBA;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import com.google.common.eventbus.Subscribe;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.Locale;

import static itz.silentcore.utils.render.Fonts.icons;
import static itz.silentcore.utils.render.Fonts.sf_pro;

public class InformationRenderer extends DragComponent {
    public static float CHAT_OFFSET_DISTANCE = 13f;
    public static float BASE_Y_OFFSET = 5f;
    public static float BASE_X_OFFSET = 5f;

    private float smoothedBps = 0f;
    private float smoothedY = 0f;
    private float coordsClickX = 0f;
    private float coordsClickY = 0f;
    private float coordsClickWidth = 0f;
    private float coordsClickHeight = 0f;
    private String lastCoordsText = "";

    public InformationRenderer() {
        super("info");
        setDraggable(false);
        setAllowDragX(false);
        setAllowDragY(false);
        setX(10);
        setY(0);
        SilentCore.getInstance().eventBus.register(this);
    }

    @Subscribe
    public void onMouseClick(MouseClickEvent event) {
        if (event.getButton() != 0) {
            return;
        }

        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();

        boolean xMatch = mouseX >= coordsClickX && mouseX <= coordsClickX + coordsClickWidth;
        boolean yMatch = mouseY >= coordsClickY && mouseY <= coordsClickY + coordsClickHeight;

        // Only copy if it's valid coordinates (contains only digits and commas)
        if (xMatch && yMatch && !lastCoordsText.isEmpty() && lastCoordsText.matches("[0-9,-]+")) {
            copyToClipboard(lastCoordsText);
        }
    }

    private void copyToClipboard(String text) {
        try {
            boolean success = tryAwtClipboard(text);

            if (!success) {
                String osName = System.getProperty("os.name").toLowerCase();
                if (osName.contains("win")) {
                    success = tryWindowsClipboard(text);
                } else if (osName.contains("mac")) {
                    success = tryMacClipboard(text);
                } else if (osName.contains("nux") || osName.contains("nix")) {
                    success = tryLinuxClipboard(text);
                }
            }

            if (success) {
                NotificationRenderer.pushWithIcon("Координаты скопированы", "P", itz.silentcore.utils.client.ClientUtility.getThemePrimaryColorRGBA());
            }
        } catch (Exception e) {
            NotificationRenderer.push("Ошибка копирования");
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
        var context = event.getContext();
        var mc = MinecraftClient.getInstance();

        float paddingX = 5f;
        float paddingY = 4f;
        float radius = 5.0f;
        float textSize = 8.0f;
        float iconSize = textSize + 2.0f;
        float gap = 4.0f;

        float rawBps = MoveUtil.speedSqrt() * 20.0F;

        float alpha = 0.2f;
        smoothedBps += (rawBps - smoothedBps) * alpha;
        if (Float.isNaN(smoothedBps) || Float.isInfinite(smoothedBps)) smoothedBps = 0f;

        float bpsDisplay = Math.round(smoothedBps * 10.0F) / 10.0F;
        String bpsText = String.format(Locale.US, "%.1f", bpsDisplay) + " bps";

        String coordsText = "0,0,0";
        if (mc.player != null) {
            int cx = (int) Math.floor(mc.player.getX());
            int cy = (int) Math.floor(mc.player.getY());
            int cz = (int) Math.floor(mc.player.getZ());
            coordsText = cx + "," + cy + "," + cz;
        }

        float iconLeftWidth = icons.getWidth("J", iconSize);
        float bpsWidth = sf_pro.getWidth(bpsText, textSize);
        float iconRightWidth = icons.getWidth("j", iconSize);
        float coordsWidth = sf_pro.getWidth(coordsText, textSize);

        float textHeight = sf_pro.getMetrics().lineHeight() * textSize;
        float iconHeight = icons.getMetrics().lineHeight() * iconSize;
        float contentHeight = Math.max(textHeight, iconHeight);

        float width = paddingX * 2 + iconLeftWidth + gap + bpsWidth + gap + iconRightWidth + gap + coordsWidth;
        float height = paddingY * 2 + contentHeight;

        float x = BASE_X_OFFSET;
        float baseY = context.getContext().getScaledWindowHeight() - height - BASE_Y_OFFSET;

        // Calculate target Y based on chat open state
        boolean chatOpen = mc.currentScreen instanceof ChatScreen;
        float targetY = chatOpen ? baseY - CHAT_OFFSET_DISTANCE : baseY;

        // Smooth animation for Y position
        float smoothingAlpha = 0.15f;
        smoothedY += (targetY - smoothedY) * smoothingAlpha;

        float y = smoothedY;

        context.drawBlur(x, y, width, height, radius, 30, ColorRGBA.of(255, 255, 255, 255));
        context.drawRect(x, y, width, height, radius, ColorRGBA.of(0, 0, 0, 166));

        float centerY = y + height / 2f;
        float iconY = centerY - iconHeight / 2f;
        float textY = centerY - textHeight / 2f;

        ColorRGBA themeColor = itz.silentcore.utils.client.ClientUtility.getThemePrimaryColorRGBA();
        float cursorX = x + paddingX;
        context.drawText("J", icons, cursorX, iconY, iconSize, themeColor);
        cursorX += iconLeftWidth + gap;

        context.drawText(bpsText, sf_pro, cursorX, textY, textSize, ColorRGBA.of(255, 255, 255));
        cursorX += bpsWidth + gap;

        context.drawText("j", icons, cursorX, iconY, iconSize, themeColor);
        cursorX += iconRightWidth + gap;

        coordsClickX = cursorX;
        coordsClickY = smoothedY;
        coordsClickWidth = coordsWidth;
        coordsClickHeight = height;
        lastCoordsText = coordsText;

        context.drawText(coordsText, sf_pro, cursorX, textY, textSize, ColorRGBA.of(255, 255, 255));

        setX(x);
        setY(smoothedY);
        setWidth(width);
        setHeight(height);
    }
}

