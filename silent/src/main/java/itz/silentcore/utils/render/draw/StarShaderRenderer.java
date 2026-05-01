package itz.silentcore.utils.render.draw;

import com.mojang.blaze3d.systems.RenderSystem;
import itz.silentcore.utils.render.providers.ResourceProvider;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;

public class StarShaderRenderer {
    private static final ShaderProgramKey STAR_SHADER_KEY = new ShaderProgramKey(
            ResourceProvider.getShaderIdentifier("starguy"),
            VertexFormats.POSITION,
            Defines.EMPTY
    );

    public static void render(float width, float height) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        ShaderProgram shader = RenderSystem.setShader(STAR_SHADER_KEY);
        
        // Проверяем что шейдер загружен
        if (shader == null) {
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            return;
        }
        
        // Устанавливаем uniform переменные
        float time = (System.currentTimeMillis() % 1000000) / 1000.0f;
        if (shader.getUniform("time") != null) {
            shader.getUniform("time").set(time);
        }
        if (shader.getUniform("resolution") != null) {
            shader.getUniform("resolution").set(width, height);
        }

        // Рисуем полноэкранный квад
        Matrix4f matrix = new Matrix4f().identity();
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        
        bufferBuilder.vertex(matrix, 0, height, 0);
        bufferBuilder.vertex(matrix, width, height, 0);
        bufferBuilder.vertex(matrix, width, 0, 0);
        bufferBuilder.vertex(matrix, 0, 0, 0);

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
