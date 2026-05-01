package com.example.client.utils.render.text;

import net.minecraft.client.render.VertexConsumer;
import org.joml.Matrix4f;

public final class Glyph {
    private final float minU, maxU, minV, maxV;
    private final float advance, topPosition, width, height;

    public Glyph(FontData.GlyphData data, float atlasWidth, float atlasHeight) {
        this.advance = data.advance();

        FontData.BoundsData atlasBounds = data.atlasBounds();
        if (atlasBounds != null) {
            this.minU = atlasBounds.left() / atlasWidth;
            this.maxU = atlasBounds.right() / atlasWidth;
            this.minV = 1.0f - atlasBounds.top() / atlasHeight;
            this.maxV = 1.0f - atlasBounds.bottom() / atlasHeight;
        } else {
            this.minU = this.maxU = this.minV = this.maxV = 0.0f;
        }

        FontData.BoundsData planeBounds = data.planeBounds();
        if (planeBounds != null) {
            this.width = planeBounds.right() - planeBounds.left();
            this.height = planeBounds.top() - planeBounds.bottom();
            this.topPosition = planeBounds.top();
        } else {
            this.width = this.height = this.topPosition = 0.0f;
        }
    }

    public float apply(Matrix4f matrix, VertexConsumer consumer, float size, float x, float y, float z, int color) {
        float ry = y - this.topPosition * size;
        float w = this.width * size;
        float h = this.height * size;
        consumer.vertex(matrix, x, ry, z).texture(minU, minV).color(color);
        consumer.vertex(matrix, x, ry + h, z).texture(minU, maxV).color(color);
        consumer.vertex(matrix, x + w, ry + h, z).texture(maxU, maxV).color(color);
        consumer.vertex(matrix, x + w, ry, z).texture(maxU, minV).color(color);
        return this.advance * size;
    }

    public float getWidth(float size) {
        return this.advance * size;
    }
}
