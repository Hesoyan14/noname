package com.example.client.utils.render.draw;

import com.example.client.utils.render.providers.ColorProvider;
import com.example.client.utils.render.providers.ResourceProvider;
import com.example.client.utils.render.text.Font;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;

public class TextRenderer {
    private static final ShaderProgramKey KEY = new ShaderProgramKey(
            ResourceProvider.getShaderIdentifier("font"),
            VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY);

    public static void draw(Matrix4f matrix, float x, float y,
                            Font font, String text,
                            float size, float thickness,
                            float smoothness, float spacing,
                            int color) {
        if (text == null || text.isBlank()) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShaderTexture(0, font.getTextureId());

        ShaderProgram shader = RenderSystem.setShader(KEY);
        shader.getUniform("Range").set(font.getAtlas().range());
        shader.getUniform("Thickness").set(thickness);
        shader.getUniform("Smoothness").set(smoothness);
        shader.getUniform("Outline").set(0);

        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        font.applyGlyphs(matrix, buf, text, size,
                thickness * 0.5f * size, spacing,
                x, y + font.getMetrics().baselineHeight() * size, 0, color);

        BufferRenderer.drawWithGlobalProgram(buf.end());

        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
