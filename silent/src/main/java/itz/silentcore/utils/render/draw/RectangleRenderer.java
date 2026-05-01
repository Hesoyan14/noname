package itz.silentcore.utils.render.draw;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

import itz.silentcore.utils.render.providers.ResourceProvider;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;

public class RectangleRenderer {
    private static final ShaderProgramKey RECTANGLE_SHADER_KEY = new ShaderProgramKey(
        ResourceProvider.getShaderIdentifier("rectangle"),
        VertexFormats.POSITION_COLOR, Defines.EMPTY);

    public static void draw(Matrix4f matrix, float x, float y, float z,
                            float width, float height,
                            float radiusTopLeft, float radiusBottomLeft,
                            float radiusBottomRight, float radiusTopRight,
                            int colorTopLeft, int colorBottomLeft,
                            int colorBottomRight, int colorTopRight,
                            float smoothness) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        ShaderProgram shader = RenderSystem.setShader(RECTANGLE_SHADER_KEY);
        shader.getUniform("Size").set(width, height);
        shader.getUniform("Radius").set(radiusTopLeft, radiusBottomLeft, radiusBottomRight, radiusTopRight);
        shader.getUniform("Smoothness").set(smoothness);

        BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(matrix, x, y, z).color(colorTopLeft);
        builder.vertex(matrix, x, y + height, z).color(colorBottomLeft);
        builder.vertex(matrix, x + width, y + height, z).color(colorBottomRight);
        builder.vertex(matrix, x + width, y, z).color(colorTopRight);

        BufferRenderer.drawWithGlobalProgram(builder.end());

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static void draw(Matrix4f matrix, float x, float y, float width, float height,
                            float radius, int color, float smoothness) {
        draw(matrix, x, y, 0.0f, width, height,
            radius, radius, radius, radius,
            color, color, color, color,
            smoothness);
    }
}