package com.example.client.utils.render.draw;

import com.example.client.utils.render.providers.ResourceProvider;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;

public class BorderRenderer {
    private static final ShaderProgramKey KEY = new ShaderProgramKey(
            ResourceProvider.getShaderIdentifier("border"),
            VertexFormats.POSITION_COLOR, Defines.EMPTY);

    public static void draw(Matrix4f matrix, float x, float y, float z,
                            float width, float height,
                            float rTL, float rBL, float rBR, float rTR,
                            int cTL, int cBL, int cBR, int cTR,
                            float thickness, float innerSmooth, float outerSmooth) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        ShaderProgram shader = RenderSystem.setShader(KEY);
        shader.getUniform("Size").set(width, height);
        shader.getUniform("Radius").set(rTL, rBL, rBR, rTR);
        shader.getUniform("Thickness").set(thickness);
        shader.getUniform("Smoothness").set(innerSmooth, outerSmooth);

        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(matrix, x, y, z).color(cTL);
        buf.vertex(matrix, x, y + height, z).color(cBL);
        buf.vertex(matrix, x + width, y + height, z).color(cBR);
        buf.vertex(matrix, x + width, y, z).color(cTR);
        BufferRenderer.drawWithGlobalProgram(buf.end());

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static void draw(Matrix4f matrix, float x, float y, float width, float height,
                            float radius, int color, float thickness,
                            float innerSmooth, float outerSmooth) {
        draw(matrix, x, y, 0f, width, height,
                radius, radius, radius, radius,
                color, color, color, color,
                thickness, innerSmooth, outerSmooth);
    }
}
