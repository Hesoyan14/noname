package itz.silentcore.utils.render.draw;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

import itz.silentcore.utils.render.text.Font;
import itz.silentcore.utils.render.providers.ColorProvider;
import itz.silentcore.utils.render.providers.ResourceProvider;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;

public class TextRenderer {
    private static final ShaderProgramKey MSDF_FONT_SHADER_KEY = new ShaderProgramKey(
        ResourceProvider.getShaderIdentifier("font"),
        VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY);

    public static void draw(Matrix4f matrix, float x, float y, float z,
                            Font font, String text,
                            float size, float thickness,
                            float smoothness, float spacing,
                            int color,
                            int outlineColor, float outlineThickness) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        RenderSystem.setShaderTexture(0, font.getTextureId());

        boolean outlineEnabled = outlineThickness > 0.0f;
        ShaderProgram shader = RenderSystem.setShader(MSDF_FONT_SHADER_KEY);
        shader.getUniform("Range").set(font.getAtlas().range());
        shader.getUniform("Thickness").set(thickness);
        shader.getUniform("Smoothness").set(smoothness);
        shader.getUniform("Outline").set(outlineEnabled ? 1 : 0);

        if (outlineEnabled) {
            shader.getUniform("OutlineThickness").set(outlineThickness);
            float[] outlineComponents = ColorProvider.normalize(outlineColor);
            shader.getUniform("OutlineColor").set(
                outlineComponents[0], outlineComponents[1],
                outlineComponents[2], outlineComponents[3]);
        }

        BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        font.applyGlyphs(matrix, builder, text, size,
            (thickness + outlineThickness * 0.5f) * 0.5f * size, spacing,
            x, y + font.getMetrics().baselineHeight() * size, z, color);

        if (text.isBlank()) return;
        BufferRenderer.drawWithGlobalProgram(builder.end());

        RenderSystem.setShaderTexture(0, 0);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static void draw(Matrix4f matrix, float x, float y,
                            Font font, String text,
                            float size, float thickness,
                            float smoothness, float spacing,
                            int color) {
        draw(matrix, x, y, 0.0f, font, text, size, thickness, smoothness, spacing, color, 0, 0.0f);
    }
}