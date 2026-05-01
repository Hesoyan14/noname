package itz.silentcore.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Render2D {
    
    private static final List<Quad> QUAD = new ArrayList<>();
    
    public static void onRender(DrawContext context) {
        if (!QUAD.isEmpty()) {
            MatrixStack matrix = context.getMatrices();
            Matrix4f matrix4f = matrix.peek().getPositionMatrix();
            
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            
            BufferBuilder buffer = Tessellator.getInstance().begin(
                    VertexFormat.DrawMode.QUADS, 
                    VertexFormats.POSITION_COLOR
            );
            
            QUAD.forEach(quad -> {
                buffer.vertex(matrix4f, quad.x, quad.y, 0).color(quad.color);
                buffer.vertex(matrix4f, quad.x, quad.y + quad.height, 0).color(quad.color);
                buffer.vertex(matrix4f, quad.x + quad.width, quad.y + quad.height, 0).color(quad.color);
                buffer.vertex(matrix4f, quad.x + quad.width, quad.y, 0).color(quad.color);
            });
            
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.disableBlend();
            QUAD.clear();
        }
    }
    
    public static void drawQuad(float x, float y, float width, float height, int color) {
        QUAD.add(new Quad(x, y, width, height, color));
    }
    
    public static void drawTexture(MatrixStack matrix, Identifier texture, float x, float y, float width, float height, int color) {
        drawTexture(matrix, texture, x, y, width, height, 0, 0, 1, 1, color);
    }
    
    public static void drawTexture(MatrixStack matrix, Identifier texture, float x, float y, float width, float height, float u1, float v1, float u2, float v2, int color) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        BufferBuilder buffer = Tessellator.getInstance().begin(
                VertexFormat.DrawMode.QUADS, 
                VertexFormats.POSITION_TEXTURE_COLOR
        );
        
        Matrix4f matrix4f = matrix.peek().getPositionMatrix();
        buffer.vertex(matrix4f, x, y, 0).texture(u1, v1).color(color);
        buffer.vertex(matrix4f, x, y + height, 0).texture(u1, v2).color(color);
        buffer.vertex(matrix4f, x + width, y + height, 0).texture(u2, v2).color(color);
        buffer.vertex(matrix4f, x + width, y, 0).texture(u2, v1).color(color);
        
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
    }
    
    public static void drawCircle(MatrixStack matrix, float x, float y, float radius, int color) {
        int segments = 32;
        float angleStep = (float) (2 * Math.PI / segments);
        Matrix4f matrix4f = matrix.peek().getPositionMatrix();
        
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        
        BufferBuilder buffer = Tessellator.getInstance().begin(
                VertexFormat.DrawMode.TRIANGLE_FAN, 
                VertexFormats.POSITION_COLOR
        );
        
        buffer.vertex(matrix4f, x, y, 0).color(color);
        
        for (int i = 0; i <= segments; i++) {
            float angle = i * angleStep;
            float px = x + radius * (float) Math.cos(angle);
            float py = y + radius * (float) Math.sin(angle);
            buffer.vertex(matrix4f, px, py, 0).color(color);
        }
        
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
    }
    
    public static Color applyOpacity(Color color, float opacity) {
        opacity = Math.min(1, Math.max(0, opacity));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity));
    }
    
    public static int applyOpacity(int colorInt, float opacity) {
        opacity = Math.min(1, Math.max(0, opacity));
        Color color = new Color(colorInt, true);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity)).getRGB();
    }
    
    public record Quad(float x, float y, float width, float height, int color) {}
}
