package itz.silentcore.utils.render.draw;

import org.joml.Matrix4f;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.RenderSystem;

import itz.silentcore.utils.render.providers.ResourceProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;

public class BlurRenderer {
    private static final ShaderProgramKey KAWASE_SHADER_KEY = new ShaderProgramKey(
        ResourceProvider.getShaderIdentifier("blur"),
        VertexFormats.POSITION_COLOR, Defines.EMPTY);

    private static final Supplier<SimpleFramebuffer> TEMP_FBO_SUPPLIER = Suppliers
        .memoize(() -> new SimpleFramebuffer(1920, 1024, false));
    private static final Framebuffer MAIN_FBO = MinecraftClient.getInstance().getFramebuffer();

    public static void draw(Matrix4f matrix, float x, float y, float width, float height, float radius,
                            int colorTopLeft, int colorBottomLeft, int colorBottomRight, int colorTopRight,
                            float blurRadius, float smoothness) {
        draw(matrix, x, y, 0.0f,
            width, height,
            radius, radius, radius, radius,
            colorTopLeft, colorBottomLeft, colorBottomRight, colorTopRight,
            blurRadius, smoothness);
    }

    public static void draw(Matrix4f matrix, float x, float y, float z,
                            float width, float height,
                            float radiusTopLeft, float radiusBottomLeft,
                            float radiusBottomRight, float radiusTopRight,
                            int colorTopLeft, int colorBottomLeft,
                            int colorBottomRight, int colorTopRight,
                            float blurRadius, float smoothness) {
        SimpleFramebuffer fbo = TEMP_FBO_SUPPLIER.get();
        if (fbo.textureWidth != MAIN_FBO.textureWidth || fbo.textureHeight != MAIN_FBO.textureHeight) {
            fbo.resize(MAIN_FBO.textureWidth, MAIN_FBO.textureHeight);
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        fbo.beginWrite(false);
        MAIN_FBO.draw(fbo.textureWidth, fbo.textureHeight);
        MAIN_FBO.beginWrite(false);

        RenderSystem.setShaderTexture(0, fbo.getColorAttachment());

        ShaderProgram shader = RenderSystem.setShader(KAWASE_SHADER_KEY);
        shader.getUniform("Size").set(width, height);
        shader.getUniform("Radius").set(radiusTopLeft, radiusBottomLeft, radiusBottomRight, radiusTopRight);
        shader.getUniform("Smoothness").set(smoothness);

        float radiusValue = Math.max(0.5f, Math.abs(blurRadius));
        int iterations = Math.max(1, Math.min(16, Math.round(radiusValue)));
        float step = radiusValue / iterations;

        shader.getUniform("BlurRadius").set(step);
        shader.getUniform("Iterations").set(iterations);

        BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(matrix, x, y, z).color(colorTopLeft);
        builder.vertex(matrix, x, y + height, z).color(colorBottomLeft);
        builder.vertex(matrix, x + width, y + height, z).color(colorBottomRight);
        builder.vertex(matrix, x + width, y, z).color(colorTopRight);

        BufferRenderer.drawWithGlobalProgram(builder.end());

        RenderSystem.setShaderTexture(0, 0);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}