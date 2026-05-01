package itz.silentcore.utils.client;

import itz.silentcore.feature.command.impl.ThemeCommand;
import itz.silentcore.utils.render.ColorRGBA;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@UtilityClass
public class ClientUtility implements IMinecraft {

    private static final Pattern HEX_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");
    private static int[] cachedThemeColors = null;
    @Getter
    private static String currentTheme = "CANDY";
    private static long tpsBaseWorldTime = -1L;
    private static long tpsBaseMs = -1L;
    private static float smoothedTps = 20.0f;

    public static int getFPS() {
        return mc.getCurrentFps();
    }

    public static String getNickname() {
        return mc.getSession() != null ? mc.getSession().getUsername() : "Unknown";
    }

    public static String getVersion() {
        return net.minecraft.SharedConstants.getGameVersion().getName();
    }

    public static String getTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public static float getTPS() {
        try {
            if (mc == null || mc.world == null) return 0.0f;

            long nowMs = System.currentTimeMillis();
            long worldTime = mc.world.getTime();

            if (tpsBaseWorldTime < 0 || tpsBaseMs < 0) {
                tpsBaseWorldTime = worldTime;
                tpsBaseMs = nowMs;
                return smoothedTps;
            }

            long windowMs = 1000L;
            long elapsedMs = nowMs - tpsBaseMs;
            if (elapsedMs >= windowMs) {
                long elapsedTicks = Math.max(0L, worldTime - tpsBaseWorldTime);
                float instantTps = (float) (elapsedTicks / (elapsedMs / 1000.0));
                if (!Float.isFinite(instantTps)) instantTps = 0.0f;
                float clamped = Math.max(0.0f, Math.min(20.0f, instantTps));
                float alpha = 0.5f;
                smoothedTps += (clamped - smoothedTps) * alpha;

                tpsBaseWorldTime = worldTime;
                tpsBaseMs = nowMs;
            }

            return smoothedTps;
        } catch (Exception e) {
            return 0.0f;
        }
    }

    public static String getServerIP() {
        try {
            if (mc == null) return "Unknown";

            if (mc.isInSingleplayer() || mc.getServer() != null) {
                return "Одиночный мир";
            }

            if (mc.getCurrentServerEntry() != null && mc.getCurrentServerEntry().address != null) {
                return mc.getCurrentServerEntry().address;
            }

            if (mc.getNetworkHandler() != null && mc.getNetworkHandler().getConnection() != null) {
                String addr = String.valueOf(mc.getNetworkHandler().getConnection().getAddress());
                if (addr.startsWith("/")) addr = addr.substring(1);
                return addr;
            }

            return "Unknown";
        } catch (Exception e) {
            return "Unknown";
        }
    }

    public static int gradient(int startColor, int endColor, float progress) {
        progress = Math.max(0.0f, Math.min(1.0f, progress));
        int startR = (startColor >> 16) & 0xFF;
        int startG = (startColor >> 8) & 0xFF;
        int startB = startColor & 0xFF;
        int endR = (endColor >> 16) & 0xFF;
        int endG = (endColor >> 8) & 0xFF;
        int endB = endColor & 0xFF;
        int r = (int) (startR + (endR - startR) * progress);
        int g = (int) (startG + (endG - startG) * progress);
        int b = (int) (startB + (endB - startB) * progress);
        return (r << 16) | (g << 8) | b;
    }

    public static void setCurrentTheme(String themeName) {
        currentTheme = themeName.toUpperCase();
        invalidateThemeCache();
    }

    public static void invalidateThemeCache() {
        cachedThemeColors = null;
    }

    public static int getThemePrimaryColor() {
        int[] colors = getThemeGradientColors();
        return colors[0];
    }

    public static ColorRGBA getThemePrimaryColorRGBA() {
        // Используем новый ThemeManager вместо старой системы
        return itz.silentcore.feature.theme.ThemeManager.getInstance().getPrimaryColorRGBA();
    }

    private static int[] getThemeGradientColors() {
        if (cachedThemeColors != null) {
            return cachedThemeColors;
        }

        String[] colors = ThemeCommand.getThemeColors(currentTheme);
        int startColor = 0xB387F5;
        int endColor = 0xBF95FF;

        if (colors != null && colors.length >= 1) {
            try {
                if (HEX_PATTERN.matcher(colors[0]).matches()) {
                    startColor = Integer.parseInt(colors[0].substring(1), 16);
                }
                if (colors.length > 1 && HEX_PATTERN.matcher(colors[1]).matches()) {
                    endColor = Integer.parseInt(colors[1].substring(1), 16);
                } else {
                    endColor = startColor;
                }
            } catch (Exception ignored) {}
        }

        cachedThemeColors = new int[]{startColor, endColor};
        return cachedThemeColors;
    }

    public static void sendMessage(String message) {
        if (message == null || mc.player == null) return;

        MutableText finalText = Text.literal("");
        String prefix = Constants.PREFIX;
        int[] colors = getThemeGradientColors();
        int startColor = colors[0], endColor = colors[1];

        for (int i = 0; i < prefix.length(); i++) {
            float progress = (float) i / (prefix.length() - 1);
            int color = gradient(startColor, endColor, progress);
            finalText.append(Text.literal(String.valueOf(prefix.charAt(i)))
                    .styled(style -> style.withColor(color)));
        }

        finalText.append(Text.literal(message));
        mc.player.sendMessage(finalText, false);
    }

    public static void sendIRCMessage(String message) {
        if (message == null || mc.player == null) return;

        MutableText finalText = Text.literal("");
        String prefix = Constants.IRC_TITLE;
        int[] colors = getThemeGradientColors();
        int startColor = colors[0], endColor = colors[1];

        for (int i = 0; i < prefix.length(); i++) {
            float progress = (float) i / (prefix.length() - 1);
            int color = gradient(startColor, endColor, progress);
            finalText.append(Text.literal(String.valueOf(prefix.charAt(i)))
                    .styled(style -> style.withColor(color)));
        }

        finalText.append(Text.literal( message));
        mc.player.sendMessage(finalText, false);
    }

    public static void sendGradientMessage(String message) {
        if (message == null || mc.player == null) return;
        int[] colors = getThemeGradientColors();
        sendGradientMessage(message, colors[0], colors[1]);
    }

    public static void sendGradientMessage(String message, int startColor, int endColor) {
        if (message == null || mc.player == null) return;

        MutableText finalText = Text.literal("");
        String[] lines = message.split("\n");

        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            String line = lines[lineIndex];
            if (line.isEmpty()) {
                finalText.append(Text.literal("\n"));
                continue;
            }

            for (int i = 0; i < line.length(); i++) {
                float progress = line.length() > 1 ? (float) i / (line.length() - 1) : 0.0f;
                int color = gradient(startColor, endColor, progress);
                finalText.append(Text.literal(String.valueOf(line.charAt(i)))
                        .styled(style -> style.withColor(color)));
            }

            if (lineIndex < lines.length - 1) {
                finalText.append(Text.literal("\n"));
            }
        }

        mc.player.sendMessage(finalText, false);
    }

    public static void sendMessageByPlayer(String message) {
        if (message == null || mc.player == null || mc.getNetworkHandler() == null) return;
        mc.getNetworkHandler().sendChatMessage(message);
    }

    public static void sendCommand(String command) {
        if (command == null || command.isEmpty()) return;
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        mc.getNetworkHandler().sendChatCommand(command);
    }

    public static String getServer() {
        IntegratedServer server = mc.getServer();
        return server == null ? "Одиночный мир" : server.getSaveProperties().getLevelName();
    }
}
