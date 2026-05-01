package com.example.client.utils.render.text;

import com.example.client.utils.render.providers.ResourceProvider;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class Font {
    private final String name;
    private final AbstractTexture texture;
    private final FontData.AtlasData atlas;
    private final FontData.MetricsData metrics;
    private final Map<Integer, Glyph> glyphs;
    private final Map<Integer, Map<Integer, Float>> kernings;

    private Font(String name, AbstractTexture texture, FontData.AtlasData atlas,
                 FontData.MetricsData metrics, Map<Integer, Glyph> glyphs,
                 Map<Integer, Map<Integer, Float>> kernings) {
        this.name = name;
        this.texture = texture;
        this.atlas = atlas;
        this.metrics = metrics;
        this.glyphs = glyphs;
        this.kernings = kernings;
    }

    public String getName() { return name; }
    public FontData.AtlasData getAtlas() { return atlas; }
    public FontData.MetricsData getMetrics() { return metrics; }
    public int getTextureId() { return texture.getGlId(); }

    public void applyGlyphs(Matrix4f matrix, VertexConsumer consumer, String text,
                            float size, float thickness, float spacing,
                            float x, float y, float z, int color) {
        int prevChar = -1;
        for (int i = 0; i < text.length(); i++) {
            int ch = text.charAt(i);
            Glyph glyph = glyphs.get(ch);
            if (glyph == null) continue;
            Map<Integer, Float> kern = kernings.get(prevChar);
            if (kern != null) x += kern.getOrDefault(ch, 0.0f) * size;
            x += glyph.apply(matrix, consumer, size, x, y, z, color) + thickness + spacing;
            prevChar = ch;
        }
    }

    public float getWidth(String text, float size) {
        int prevChar = -1;
        float width = 0.0f;
        for (int i = 0; i < text.length(); i++) {
            int ch = text.charAt(i);
            Glyph glyph = glyphs.get(ch);
            if (glyph == null) continue;
            Map<Integer, Float> kern = kernings.get(prevChar);
            if (kern != null) width += kern.getOrDefault(ch, 0.0f) * size;
            width += glyph.getWidth(size);
            prevChar = ch;
        }
        return width;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String name = "?";
        private Identifier dataId;
        private Identifier atlasId;

        public Builder name(String name) { this.name = name; return this; }

        public Builder data(String fileName) {
            this.dataId = Identifier.of("noname", "fonts/" + fileName + ".json");
            return this;
        }

        public Builder atlas(String fileName) {
            this.atlasId = Identifier.of("noname", "fonts/" + fileName + ".png");
            return this;
        }

        public Font build() {
            FontData data = ResourceProvider.fromJsonToInstance(dataId, FontData.class);
            AbstractTexture texture = MinecraftClient.getInstance().getTextureManager().getTexture(atlasId);

            if (data == null) throw new RuntimeException("Failed to load font: " + dataId);

            RenderSystem.recordRenderCall(() -> texture.setFilter(true, false));

            float aw = data.atlas().width();
            float ah = data.atlas().height();

            Map<Integer, Glyph> glyphs = data.glyphs().stream()
                    .collect(Collectors.toMap(FontData.GlyphData::unicode, g -> new Glyph(g, aw, ah)));

            Map<Integer, Map<Integer, Float>> kernings = new HashMap<>();
            data.kernings().forEach(k -> {
                kernings.computeIfAbsent(k.leftChar(), x -> new HashMap<>()).put(k.rightChar(), k.advance());
            });

            return new Font(name, texture, data.atlas(), data.metrics(), glyphs, kernings);
        }
    }
}
