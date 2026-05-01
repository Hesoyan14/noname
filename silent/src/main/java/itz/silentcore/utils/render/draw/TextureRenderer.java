package itz.silentcore.utils.render.draw;

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
import org.joml.Matrix4f;

public class TextureRenderer {
    private static final ShaderProgramKey TEXTURE_SHADER_KEY = new ShaderProgramKey(
            ResourceProvider.getShaderIdentifier("texture"),
            VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY);

    public static void draw(Matrix4f matrix, float x, float y, float width, float height, float radius,
                            int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight,
                            float u, float v, float texWidth, float texHeight, int textureId,
                            float smoothness) {
        draw(matrix, x, y, 0.0f,
                width, height,
                radius, radius, radius, radius,
                colorTopLeft, colorBottomLeft, colorBottomRight, colorTopRight,
                u, v, texWidth, texHeight, textureId,
                smoothness);
    }

    public static void draw(Matrix4f matrix, float x, float y, float z,
                            float width, float height,
                            float radiusTopLeft, float radiusBottomLeft,
                            float radiusBottomRight, float radiusTopRight,
                            int colorTopLeft, int colorBottomLeft,
                            int colorBottomRight, int colorTopRight,
                            float u, float v, float texWidth, float texHeight, int textureId,
                            float smoothness) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        RenderSystem.setShaderTexture(0, textureId);

        ShaderProgram shader = RenderSystem.setShader(TEXTURE_SHADER_KEY);
        shader.getUniform("Size").set(width, height);
        shader.getUniform("Radius").set(radiusTopLeft, radiusBottomLeft, radiusBottomRight, radiusTopRight);
        shader.getUniform("Smoothness").set(smoothness);

        BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        builder.vertex(matrix, x, y, z).texture(u, v).color(colorTopLeft);
        builder.vertex(matrix, x, y + height, z).texture(u, v + texHeight).color(colorBottomLeft);
        builder.vertex(matrix, x + width, y + height, z).texture(u + texWidth, v + texHeight).color(colorBottomRight);
        builder.vertex(matrix, x + width, y, z).texture(u + texWidth, v).color(colorTopRight);

        BufferRenderer.drawWithGlobalProgram(builder.end());

        RenderSystem.setShaderTexture(0, 0);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}