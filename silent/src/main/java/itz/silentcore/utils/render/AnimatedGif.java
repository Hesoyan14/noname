package itz.silentcore.utils.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.awt.*;
import com.mojang.blaze3d.systems.RenderSystem;

public class AnimatedGif {
    private final Identifier source;
    private final String registryPrefix;
    private final List<Identifier> frameIds = new ArrayList<>();
    private final List<Integer> frameDurationsMs = new ArrayList<>();
    private long lastFrameChange = System.currentTimeMillis();
    private int frameIndex = 0;
    private boolean loaded = false;
    private int canvasWidth = 0;
    private int canvasHeight = 0;
    private boolean fast = false;
    private double fixedFrameMs = 0.0;
    private double lastTimeMs = 0.0;
    private double accMs = 0.0;
    private boolean playOnce = false;
    private boolean finished = false;

    public AnimatedGif(Identifier source, String registryPrefix) {
        this.source = source;
        this.registryPrefix = registryPrefix;
    }

    private void ensureLoaded() {
        if (loaded) return;

        ResourceManager rm = MinecraftClient.getInstance().getResourceManager();
        Optional<Resource> resOpt = rm.getResource(this.source);
        if (resOpt.isEmpty()) {
            loaded = true;
            return;
        }

        try (InputStream in = resOpt.get().getInputStream(); ImageInputStream iis = ImageIO.createImageInputStream(in)) {
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
            if (!readers.hasNext()) { loaded = true; return; }
            ImageReader reader = readers.next();
            reader.setInput(iis, false);

            int num = reader.getNumImages(true);
            if (num <= 0) { loaded = true; return; }

            try {
                IIOMetadata sMeta = reader.getStreamMetadata();
                if (sMeta != null) {
                    String sFmt = sMeta.getNativeMetadataFormatName();
                    IIOMetadataNode sRoot = (IIOMetadataNode) sMeta.getAsTree(sFmt);
                    IIOMetadataNode lsd = findNode(sRoot, "LogicalScreenDescriptor");
                    if (lsd != null) {
                        canvasWidth = Integer.parseInt(lsd.getAttribute("logicalScreenWidth"));
                        canvasHeight = Integer.parseInt(lsd.getAttribute("logicalScreenHeight"));
                    }
                }
            } catch (Exception ignored) {}
            if (canvasWidth <= 0 || canvasHeight <= 0) {
                try {
                    canvasWidth = reader.getWidth(0);
                    canvasHeight = reader.getHeight(0);
                } catch (Exception e) { canvasWidth = canvasHeight = 0; }
            }
            if (canvasWidth <= 0 || canvasHeight <= 0) {
                BufferedImage first = reader.read(0);
                canvasWidth = first.getWidth();
                canvasHeight = first.getHeight();
            }

            BufferedImage master = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = master.createGraphics();
            g.setComposite(AlphaComposite.Src);
            g.setBackground(new Color(0, 0, 0, 0));
            g.clearRect(0, 0, canvasWidth, canvasHeight);
            g.setComposite(AlphaComposite.SrcOver);

            for (int i = 0; i < num; i++) {
                IIOMetadata meta = reader.getImageMetadata(i);
                int delayMs = extractDelayMs(meta);

                FrameRect rect = extractFrameRect(meta);

                BufferedImage prevMaster = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D pg = prevMaster.createGraphics();
                pg.drawImage(master, 0, 0, null);
                pg.dispose();
                BufferedImage frameImage = reader.read(i);
                frameImage = toArgb(frameImage);
                removeGrayWhite(frameImage);
                int dx = (rect != null) ? rect.left : 0;
                int dy = (rect != null) ? rect.top : 0;
                g.drawImage(frameImage, dx, dy, null);
                BufferedImage snapshot = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
                snapshot.getGraphics().drawImage(master, 0, 0, null);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(snapshot, "PNG", baos);
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                NativeImage nativeImage = NativeImage.read(bais);
                NativeImageBackedTexture tex = new NativeImageBackedTexture(nativeImage);
                Identifier frameId = Identifier.of(
                        source.getNamespace(),
                        registryPrefix + "/" + source.getPath().replace('/', '_') + "_frame_" + i
                );
                RenderSystem.recordRenderCall(() -> tex.setFilter(true, false));
                MinecraftClient.getInstance().getTextureManager().registerTexture(frameId, tex);
                frameIds.add(frameId);
                frameDurationsMs.add(Math.max(delayMs, 16));
                String disposal = extractDisposal(meta);
                if ("restoreToBackgroundColor".equalsIgnoreCase(disposal) && rect != null) {
                    g.setComposite(AlphaComposite.Clear);
                    g.fillRect(rect.left, rect.top, rect.width, rect.height);
                    g.setComposite(AlphaComposite.SrcOver);
                } else if ("restoreToPrevious".equalsIgnoreCase(disposal)) {
                    g.setComposite(AlphaComposite.Src);
                    g.drawImage(prevMaster, 0, 0, null);
                    g.setComposite(AlphaComposite.SrcOver);
                }
            }
            g.dispose();
            loaded = true;
        } catch (IOException e) {
            loaded = true;
        }
    }

    private int extractDelayMs(IIOMetadata metadata) {
        try {
            String metaFormat = metadata.getNativeMetadataFormatName();
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormat);
            IIOMetadataNode gce = findNode(root, "GraphicControlExtension");
            if (gce != null) {
                String delayStr = gce.getAttribute("delayTime");
                int cs = 0;
                if (delayStr != null && !delayStr.isEmpty()) {
                    cs = Integer.parseInt(delayStr);
                }
                return cs * 10;
            }
        } catch (Exception ignored) {}
        return 100;
    }

    private String extractDisposal(IIOMetadata metadata) {
        try {
            String metaFormat = metadata.getNativeMetadataFormatName();
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormat);
            IIOMetadataNode gce = findNode(root, "GraphicControlExtension");
            if (gce != null) {
                return gce.getAttribute("disposalMethod");
            }
        } catch (Exception ignored) {}
        return "none";
    }

    private FrameRect extractFrameRect(IIOMetadata metadata) {
        try {
            String metaFormat = metadata.getNativeMetadataFormatName();
            IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormat);
            IIOMetadataNode desc = findNode(root, "ImageDescriptor");
            if (desc != null) {
                int left = Integer.parseInt(desc.getAttribute("imageLeftPosition"));
                int top = Integer.parseInt(desc.getAttribute("imageTopPosition"));
                int w = Integer.parseInt(desc.getAttribute("imageWidth"));
                int h = Integer.parseInt(desc.getAttribute("imageHeight"));
                return new FrameRect(left, top, w, h);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static final class FrameRect {
        final int left, top, width, height;
        FrameRect(int l, int t, int w, int h) { this.left = l; this.top = t; this.width = w; this.height = h; }
    }

    private IIOMetadataNode findNode(IIOMetadataNode root, String name) {
        for (int i = 0; i < root.getLength(); i++) {
            if (root.item(i) instanceof IIOMetadataNode node) {
                if (name.equals(node.getNodeName())) return node;
                IIOMetadataNode sub = findNode(node, name);
                if (sub != null) return sub;
            }
        }
        return null;
    }

    public void update() {
        ensureLoaded();
        if (frameIds.isEmpty() || finished) return;

        double now = System.nanoTime() / 1_000_000.0;
        if (fixedFrameMs > 0.0) {
            if (lastTimeMs == 0.0) lastTimeMs = now;
            double dt = now - lastTimeMs;
            if (dt < 0 || dt > 1000) dt = 0;
            lastTimeMs = now;
            accMs += dt;
            int steps = (int) (accMs / fixedFrameMs);
            if (steps > 0) {
                if (playOnce) {
                    int maxIndex = frameIds.size() - 1;
                    frameIndex = Math.min(maxIndex, frameIndex + steps);
                    if (frameIndex >= maxIndex) finished = true;
                } else {
                    frameIndex = (frameIndex + steps) % frameIds.size();
                }
                accMs -= steps * fixedFrameMs;
            }
            return;
        }
        long nowMs = System.currentTimeMillis();
        if (fast) {
            if (playOnce) {
                if (frameIndex < frameIds.size() - 1) {
                    frameIndex += 1;
                } else {
                    finished = true;
                }
            } else {
                frameIndex = (frameIndex + 1) % frameIds.size();
            }
            lastFrameChange = nowMs;
            return;
        }
        int duration = frameDurationsMs.get(frameIndex);
        if (nowMs - lastFrameChange >= duration) {
            if (playOnce) {
                if (frameIndex < frameIds.size() - 1) {
                    frameIndex += 1;
                } else {
                    finished = true;
                }
            } else {
                frameIndex = (frameIndex + 1) % frameIds.size();
            }
            lastFrameChange = nowMs;
        }
    }

    public int getCurrentGlId() {
        ensureLoaded();
        if (frameIds.isEmpty() || finished) return 0;
        Identifier id = frameIds.get(frameIndex);
        return MinecraftClient.getInstance().getTextureManager().getTexture(id).getGlId();
    }

    public AnimatedGif setFast(boolean fast) {
        this.fast = fast;
        return this;
    }

    public AnimatedGif setFixedFps(int fps) {
        if (fps <= 0) {
            this.fixedFrameMs = 0.0;
        } else {
            this.fixedFrameMs = 1000.0 / (double) fps;
        }
        this.fast = false;
        this.lastTimeMs = 0.0;
        this.accMs = 0.0;
        return this;
    }

    public AnimatedGif setPlayOnce(boolean once) {
        this.playOnce = once;
        this.finished = false;
        return this;
    }

    public int getWidth() { return this.canvasWidth; }

    public int getHeight() { return this.canvasHeight; }

    private static BufferedImage toArgb(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_INT_ARGB) return src;
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D gg = dst.createGraphics();
        gg.drawImage(src, 0, 0, null);
        gg.dispose();
        return dst;
    }


    private static void removeGrayWhite(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int[] data = img.getRGB(0, 0, w, h, null, 0, w);
        int greyTol = 24;
        int minGreyLuma = 48;
        int whiteLuma = 240;
        for (int i = 0; i < data.length; i++) {
            int c = data[i];
            int a = (c >>> 24) & 0xFF;
            int r = (c >>> 16) & 0xFF;
            int g = (c >>> 8) & 0xFF;
            int b = (c) & 0xFF;
            int max = Math.max(r, Math.max(g, b));
            int min = Math.min(r, Math.min(g, b));
            int diff = max - min;
            int lum = (r * 2126 + g * 7152 + b * 72) / 10000;
            boolean isWhiteish = lum >= whiteLuma;
            boolean isGreyish = diff <= greyTol && lum >= minGreyLuma;
            if (isWhiteish || isGreyish) {
                data[i] = (0x00FFFFFF & c);
            } else {
                data[i] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        }
        img.setRGB(0, 0, w, h, data, 0, w);
    }


}