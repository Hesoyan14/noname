package itz.silentcore.feature.ui.hud;

import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaInfo;
import dev.redstones.mediaplayerinfo.MediaPlayerInfo;
import itz.silentcore.SilentCore;
import itz.silentcore.feature.event.impl.MouseClickEvent;
import itz.silentcore.feature.event.impl.Render2DEvent;
import itz.silentcore.feature.ui.hud.drag.DragComponent;
import itz.silentcore.utils.render.ColorRGBA;
import org.joml.Quaternionf;
import itz.silentcore.utils.render.RenderContext;
import itz.silentcore.utils.render.draw.RectangleRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.eventbus.Subscribe;

import static itz.silentcore.utils.render.Fonts.sf_pro;

public class MediaPlayerRenderer extends DragComponent {

    private String trackName = null;
    private String artistsText = null;
    private float progress = 0.0f;
    private long currentTime = 0;
    private long totalTime = 1;
    private byte[] coverImageData = null;

    private IMediaSession activeSession = null;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean isPlaying = false;
    private final AtomicBoolean polling = new AtomicBoolean(false);
    private volatile long lastPollMs = 0L;

    private final Identifier coverTextureLocation = Identifier.of("silentcore", "music_cover");
    private NativeImageBackedTexture coverTexture = null;
    private int coverHash = 0;

    private float controlsTopY = 0f;
    private float controlsHeight = 0f;
    private float prevX = 0f, prevW = 0f;
    private float playX = 0f, playW = 0f;
    private float nextX = 0f, nextW = 0f;


    public MediaPlayerRenderer() {
        super("media_player");
        setDraggable(true);
        setAllowDragX(true);
        setAllowDragY(true);
        setX(715);
        setY(20);
        try {
            SilentCore.getInstance().eventBus.register(this);
        } catch (Exception ignored) {
        }
    }

    public void tick() {
        long now = System.currentTimeMillis();
        if (now - lastPollMs < 200L) return;
        lastPollMs = now;
        if (!polling.compareAndSet(false, true)) return;
        executor.execute(() -> {
            try {
                IMediaSession session = MediaPlayerInfo.Instance.getMediaSessions().stream()
                        .max(Comparator.comparing(s -> s.getMedia().getPlaying()))
                        .orElse(null);

                if (session != null) {
                    MediaInfo info = session.getMedia();
                    if (info != null && !info.getTitle().isEmpty()) {
                        String newTrackName = info.getTitle().toLowerCase();
                        String newArtistsText = (info.getArtist() != null && !info.getArtist().isEmpty())
                                ? info.getArtist().toLowerCase()
                                : "unknown artist";

                        long newCurrentTime = Math.max(0L, info.getPosition());
                        long newTotalTime = info.getDuration() > 0 ? info.getDuration() : 1;
                        boolean newPlaying = info.getPlaying();


                        byte[] newCover = info.getArtworkPng();
                        int newCoverHash = 0;
                        NativeImage decodedImage = null;
                        if (newCover != null && newCover.length > 0) {
                            try {
                                newCoverHash = Arrays.hashCode(newCover);
                                decodedImage = NativeImage.read(new ByteArrayInputStream(newCover));
                            } catch (Exception ignored) {
                                decodedImage = null;
                                newCoverHash = 0;
                            }
                        }

                        NativeImage finalDecodedImage = decodedImage;
                        int finalCoverHash = newCoverHash;
                        MinecraftClient.getInstance().execute(() -> {
                            activeSession = session;
                            trackName = newTrackName;
                            artistsText = newArtistsText;
                            totalTime = newTotalTime;
                            coverImageData = newCover;
                            currentTime = newCurrentTime;
                            progress = (float) newCurrentTime / (float) newTotalTime;
                            isPlaying = newPlaying;

                            if (newCover == null || newCover.length == 0) {
                                clearCoverTexture();
                                coverHash = 0;
                            } else {
                                if (finalDecodedImage != null) {
                                    if (finalCoverHash != coverHash) {
                                        updateCoverTexture(finalDecodedImage);
                                        coverHash = finalCoverHash;
                                    } else {
                                        try { finalDecodedImage.close(); } catch (Exception ignored) {}
                                    }
                                } else {
                                    clearCoverTexture();
                                    coverHash = 0;
                                }
                            }
                        });
                    } else {
                        MinecraftClient.getInstance().execute(this::clearData);
                    }
                } else {
                    MinecraftClient.getInstance().execute(this::clearData);
                }
            } catch (Throwable e) {
                MinecraftClient.getInstance().execute(this::clearData);
            } finally {
                polling.set(false);
            }
        });
    }

    private void clearData() {
        trackName = null;
        artistsText = null;
        progress = 0.0f;
        currentTime = 0;
        totalTime = 1;
        clearCoverTexture();
    }

    private void clearCoverTexture() {
        try {
            TextureManager tm = MinecraftClient.getInstance().getTextureManager();
            tm.destroyTexture(coverTextureLocation);
            if (coverTexture != null) {
                coverTexture.close();
                coverTexture = null;
            }
        } catch (Exception ignored) {
            coverTexture = null;
        }
        coverHash = 0;
    }

    private void updateCoverTexture(NativeImage nativeImage) {
        try {
            if (nativeImage != null) {
                TextureManager tm = MinecraftClient.getInstance().getTextureManager();
                tm.destroyTexture(coverTextureLocation);
                if (coverTexture != null) {
                    coverTexture.close();
                    coverTexture = null;
                }
                coverTexture = new NativeImageBackedTexture(nativeImage);
                tm.registerTexture(coverTextureLocation, coverTexture);
            }
        } catch (Exception e) {
            clearCoverTexture();
            try { nativeImage.close(); } catch (Exception ignored) {}
        }
    }

    private String formatTime(long time) {
        if (time < 0) time = 0;long totalSeconds = (time >= 100000) ? (time / 1000) : time;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private String ellipsizeToWidth(String text, float size, float maxWidth) {
        if (text == null) return "";
        if (maxWidth <= 0) return "";
        float full = sf_pro.getWidth(text, size);
        if (full <= maxWidth) return text;
        String ellipsis = "...";
        float ellW = sf_pro.getWidth(ellipsis, size);
        if (ellW >= maxWidth) return "";
        int end = Math.max(0, text.length() - 1);
        while (end > 0) {
            String sub = text.substring(0, end) + ellipsis;
            if (sf_pro.getWidth(sub, size) <= maxWidth) {
                return sub;
            }
            end--;
        }
        return "";
    }

    @Override
    public void render(Render2DEvent event) {
        if (trackName == null || trackName.isEmpty())
            return;

        RenderContext context = event.getContext();
        float x = getX();
        float y = getY();
        float coverSize = 16;
        float height = 30;
        float totalWidth = 220;
        float separatorX = x + 6 + coverSize + 6;
        float textX = separatorX + 6;
        float progressBarHeight = 3;
        float radius = 4.0f;
        float progressBarY = y + height - progressBarHeight;
        ColorRGBA themeColor = itz.silentcore.utils.client.ClientUtility.getThemePrimaryColorRGBA();

        float textY = y + 9;
        float trackWidth = sf_pro.getWidth(trackName, 7.5f);
        float artistsWidth = artistsText != null ? sf_pro.getWidth(artistsText, 7.5f) : 0;

        float timeIconSize = 10.0f;
        String timeIcon = "I";
        float right_timeIconW = itz.silentcore.utils.render.Fonts.icons.getWidth(timeIcon, timeIconSize);
        String currentTimeText = formatTime(currentTime);
        float right_timeTextW = sf_pro.getWidth(currentTimeText, 7.5f);
        String iconNStr = "n";
        String stateIconStr = isPlaying ? "m" : "v";
        String iconLStr = "l";
        float right_nW = itz.silentcore.utils.render.Fonts.icons.getWidth(iconNStr, timeIconSize);
        float right_stateW = itz.silentcore.utils.render.Fonts.icons.getWidth(stateIconStr, timeIconSize);
        float right_lW = itz.silentcore.utils.render.Fonts.icons.getWidth(iconLStr, timeIconSize);

        float rightSideWidthFromSep2 = 4 + right_timeIconW + 2 + right_timeTextW + 6 + 4 + right_nW + 2 + right_stateW + 2 + right_lW;

        float baseLeftFixed = (6 + coverSize + 6) + 1 + 6;
        float leftWithFullText = baseLeftFixed + trackWidth + (artistsWidth > 0 ? (3 + artistsWidth) : 0) + 8;
        float minLeftNoText = baseLeftFixed + 8;
        float minTotalIfNoText = minLeftNoText + rightSideWidthFromSep2;
        float maxPanelWidth = 220f;

        float contentWidthFull = leftWithFullText + rightSideWidthFromSep2;
        float totalWidthCalc = Math.max(minTotalIfNoText, contentWidthFull);
        if (totalWidthCalc > maxPanelWidth) totalWidthCalc = maxPanelWidth;
        totalWidth = totalWidthCalc;

        float maxTextWidth = Math.max(0, (x + totalWidth) - textX - 8 - rightSideWidthFromSep2);
        String trackDraw = trackName;
        String artistDraw = artistsText != null ? artistsText : "";
        float trackDrawW = trackWidth;
        float artistDrawW = artistsWidth;
        if (trackDrawW > maxTextWidth) {
            trackDraw = ellipsizeToWidth(trackDraw, 7.5f, maxTextWidth);
            trackDrawW = sf_pro.getWidth(trackDraw, 7.5f);
            artistDraw = "";
            artistDrawW = 0f;
        } else {
            float leftover = Math.max(0, maxTextWidth - trackDrawW);
            if (!artistDraw.isEmpty() && artistDrawW > leftover) {
                artistDraw = ellipsizeToWidth(artistDraw, 7.5f, leftover);
                artistDrawW = sf_pro.getWidth(artistDraw, 7.5f);
            }
        }

        boolean rotateSelf = itz.silentcore.feature.ui.hud.drag.DragComponent.getDragging() == this;
        if (rotateSelf) {
            var matrices = context.getContext().getMatrices();
            matrices.push();
            matrices.translate((x + totalWidth / 2f), (y + height / 2f), 0);
            matrices.multiply(new Quaternionf().rotateZ((float) Math.toRadians(getRotation())));
            matrices.translate(-(x + totalWidth / 2f), -(y + height / 2f), 0);
        }

        context.drawBlur(x, y, totalWidth, height, radius, 30, ColorRGBA.of(255, 255, 255, 255));
        context.drawRect(x, y, totalWidth, height, radius, ColorRGBA.of(0, 0, 0, 180));

        if (coverTexture != null) {
            var tex = MinecraftClient.getInstance().getTextureManager().getTexture(coverTextureLocation);
            int glId = tex != null ? tex.getGlId() : 0;
            if (glId != 0) {
                context.drawTexture(x + 6, y + 7, coverSize, coverSize, radius / 2,
                        ColorRGBA.of(255, 255, 255), 0f, 0f, 1f, 1f, glId);
            } else {
                context.drawRect(x + 6, y + 7, coverSize, coverSize, radius / 2, ColorRGBA.of(50, 50, 50, 140));
            }
        } else {
            context.drawRect(x + 6, y + 7, coverSize, coverSize, radius / 2, ColorRGBA.of(50, 50, 50, 140));
        }

        context.drawRect(separatorX, y + 5, 1, height - progressBarHeight - 9, 0.7f, ColorRGBA.of(255, 255, 255, 80));

        
        
        context.drawText(trackDraw, sf_pro, textX, textY, 7.5f, ColorRGBA.of(255, 255, 255));
        if (!artistDraw.isEmpty()) {
            context.drawText(artistDraw, sf_pro, textX + trackDrawW + 3, textY, 7.5f, ColorRGBA.of(180, 180, 180));
        }

        float separator2X = textX + trackDrawW + artistDrawW + 8;
        context.drawRect(separator2X, y + 5, 1, height - progressBarHeight - 9, 0.7f, ColorRGBA.of(255, 255, 255, 80));
        float timeIconX = separator2X + 4;
        float contentTop = y + 4.5f;
        float contentHeight = height - progressBarHeight - 9;
        float timeIconY = contentTop + (contentHeight - timeIconSize) / 2f;
        context.drawText(timeIcon, itz.silentcore.utils.render.Fonts.icons, timeIconX, timeIconY, timeIconSize, themeColor);

        float timeTextX = timeIconX + right_timeIconW + 2;
        context.drawText(currentTimeText, sf_pro, timeTextX, textY, 7.5f, ColorRGBA.of(255, 255, 255));

        float separator3X = timeTextX + sf_pro.getWidth(currentTimeText, 7.5f) + 6;
        context.drawRect(separator3X, y + 5, 1, height - progressBarHeight - 9, 0.7f, ColorRGBA.of(255, 255, 255, 80));

        float statusStartX = separator3X + 4;
        String iconN = iconNStr;
        context.drawText(iconN, itz.silentcore.utils.render.Fonts.icons, statusStartX, timeIconY, timeIconSize, ColorRGBA.of(180, 180, 180));
        String stateIcon = stateIconStr;
        float stateX = statusStartX + right_nW + 2;
        context.drawText(stateIcon, itz.silentcore.utils.render.Fonts.icons, stateX, timeIconY, timeIconSize, themeColor);
        String iconL2 = iconLStr;
        float lX = stateX + right_stateW + 2;
        context.drawText(iconL2, itz.silentcore.utils.render.Fonts.icons, lX, timeIconY, timeIconSize, ColorRGBA.of(180, 180, 180));

        controlsTopY = contentTop;
        controlsHeight = contentHeight;
        prevX = statusStartX;
        prevW = right_nW;
        playX = stateX;
        playW = right_stateW;
        nextX = lX;
        nextW = right_lW;

        RectangleRenderer.draw(
                context.getContext().getMatrices().peek().getPositionMatrix(),
                x, progressBarY, 0,
                totalWidth, progressBarHeight,
                radius, radius, radius, radius,
                themeColor.withAlpha(50).getRGB(),
                themeColor.withAlpha(50).getRGB(),
                themeColor.withAlpha(50).getRGB(),
                themeColor.withAlpha(50).getRGB(),
                0.6f
        );

        float filledWidth = totalWidth * Math.min(1.0f, progress);
        if (filledWidth > 0) {
            RectangleRenderer.draw(
                    context.getContext().getMatrices().peek().getPositionMatrix(),
                    x, progressBarY, 0,
                    filledWidth, progressBarHeight,
                    radius, radius, 0, 0,
                    themeColor.getRGB(),
                    themeColor.getRGB(),
                    themeColor.getRGB(),
                    themeColor.getRGB(),
                    0.6f
            );
        }

        setWidth(totalWidth);
        setHeight(height);

        if (rotateSelf) {
            var matrices = context.getContext().getMatrices();
            matrices.pop();
        }
    }

    @Subscribe
    public void onMouseClick(MouseClickEvent event) {
        if (event.getButton() != 0) return;
        if (trackName == null || trackName.isEmpty()) return;

        double mouseX = event.getMouseX();
        double mouseY = event.getMouseY();

        float x = getX();
        float y = getY();
        float w = getWidth();
        float h = getHeight();

        if (!(mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h)) return;

        boolean yMatch = mouseY >= controlsTopY && mouseY <= controlsTopY + controlsHeight;
        if (!yMatch) return;

        try {
            IMediaSession session = activeSession;
            if (session == null) return;

            if (mouseX >= prevX && mouseX <= prevX + prevW) {
                session.previous();
                return;
            }
            if (mouseX >= playX && mouseX <= playX + playW) {
                try {
                    session.playPause();
                } catch (Throwable t) {
                    if (isPlaying) session.pause(); else session.play();
                }
                return;
            }
            if (mouseX >= nextX && mouseX <= nextX + nextW) {
                session.next();
            }
        } catch (Throwable ignored) {
        }
    }
}
