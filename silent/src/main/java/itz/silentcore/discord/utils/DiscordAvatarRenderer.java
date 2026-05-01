package itz.silentcore.discord.utils;

import itz.silentcore.utils.render.ColorRGBA;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class DiscordAvatarRenderer {
    private static final Map<String, byte[]> AVATAR_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Integer> LOADING_STATUS = new ConcurrentHashMap<>();

    private static final Identifier FALLBACK_AVATAR = Identifier.of("silentcore", "textures/icon.png");

    private static final int AVATAR_SIZE = 256;
    private static final ColorRGBA PLACEHOLDER_COLOR = ColorRGBA.of(100, 100, 100, 255);

    public static int loadAvatarTexture(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return -1;
        }

        if (AVATAR_CACHE.containsKey(avatarUrl)) {
            return 1;
        }

        if (LOADING_STATUS.containsKey(avatarUrl)) {
            return LOADING_STATUS.get(avatarUrl);
        }

        LOADING_STATUS.put(avatarUrl, 0);
        new Thread(() -> {
            try {
                byte[] imageData = downloadAvatarData(avatarUrl);
                if (imageData != null && imageData.length > 0) {
                    AVATAR_CACHE.put(avatarUrl, imageData);
                    LOADING_STATUS.put(avatarUrl, 1);
                } else {
                    LOADING_STATUS.put(avatarUrl, -1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LOADING_STATUS.put(avatarUrl, -1);
            }
        }, "Discord-Avatar-Loader").start();

        return 0;
    }

    private static byte[] downloadAvatarData(String avatarUrl) {
        try {
            URL url = new URL(avatarUrl + "?size=256");
            InputStream inputStream = url.openStream();
            byte[] imageData = inputStream.readAllBytes();
            inputStream.close();
            return imageData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] getAvatarData(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return null;
        }
        return AVATAR_CACHE.get(avatarUrl);
    }

    public static Identifier getFallbackAvatarId() {
        return FALLBACK_AVATAR;
    }

    public static boolean isAvatarLoaded(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return false;
        }
        Integer status = LOADING_STATUS.get(avatarUrl);
        return status != null && status == 1;
    }

    public static boolean isAvatarLoading(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return false;
        }
        Integer status = LOADING_STATUS.get(avatarUrl);
        return status != null && status == 0;
    }

    public static boolean isAvatarFailed(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return true;
        }
        Integer status = LOADING_STATUS.get(avatarUrl);
        return status != null && status == -1;
    }

    public static void clearCache() {
        AVATAR_CACHE.clear();
        LOADING_STATUS.clear();
    }

    public static int getAvatarSize() {
        return AVATAR_SIZE;
    }

    public static ColorRGBA getPlaceholderColor() {
        return PLACEHOLDER_COLOR;
    }
}
