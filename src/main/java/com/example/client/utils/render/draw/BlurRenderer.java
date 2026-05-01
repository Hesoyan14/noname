package com.example.client.utils.render.draw;

import com.example.client.utils.render.providers.ResourceProvider;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;

public class BlurRenderer {
    private static final ShaderProgramKey KEY = new ShaderProgramKey(
            ResourceProvider.getShaderIdentifier("blur"),
            VertexFormats.POSITION_COLOR, Defines.EMPTY);

    // Ленивая инициализация — создаём только при первом вызове
    private static SimpleFramebuffer tempFbo = null;

    private static SimpleFramebuffer getTempFbo() {
        Framebuffer main = MinecraftClient.getInstance().getFramebuffer();
        if (tempFbo == null) {
            tempFbo = new SimpleFramebuffer(main.textureWidth, main.textureHeight, false);
        } else if (tempFbo.textureWidth != main.textureWidth || tempFbo.textureHeight != main.textureHeight) {
            tempFbo.resize(main.textureWidth, main.textureHeight);
        }
        return tempFbo;
    }

    public static void draw(Matrix4f matrix, float x, float y, float width, float height, float radius,
                            int cTL, int cBL, int cBR, int cTR,
                            float blurRadius, float smoothness) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Framebuffer mainFbo = mc.getFramebuffer();
        SimpleFramebuffer fbo = getTempFbo();

        // Копируем текущий фрейм в temp FBO
        fbo.beginWrite(false);
        mainFbo.draw(fbo.textureWidth, fbo.textureHeight);
        mainFbo.beginWrite(false);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        RenderSystem.setShaderTexture(0, fbo.getColorAttachment());

        ShaderProgram shader = RenderSystem.setShader(KEY);
        shader.getUniform("Size").set(width, height);
        shader.getUniform("Radius").set(radius, radius, radius, radius);
        shader.getUniform("Smoothness").set(smoothness);

        float r = Math.max(0.5f, Math.abs(blurRadius));
        int iterations = Math.max(1, Math.min(16, Math.round(r)));
        shader.getUniform("BlurRadius").set(r / iterations);
        shader.getUniform("Iterations").set(iterations);

        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(matrix, x, y, 0).color(cTL);
        buf.vertex(matrix, x, y + height, 0).color(cBL);
        buf.vertex(matrix, x + width, y + height, 0).color(cBR);
        buf.vertex(matrix, x + width, y, 0).color(cTR);
        BufferRenderer.drawWithGlobalProgram(buf.end());

        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
