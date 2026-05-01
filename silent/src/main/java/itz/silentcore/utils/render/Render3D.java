package itz.silentcore.utils.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4i;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Render3D {
    
    public static final List<Texture> TEXTURE_DEPTH = new ArrayList<>();
    public static final List<Texture> TEXTURE = new ArrayList<>();
    public static final List<Line> LINE_DEPTH = new ArrayList<>();
    public static final List<Line> LINE = new ArrayList<>();
    public static final List<Quad> QUAD_DEPTH = new ArrayList<>();
    public static final List<Quad> QUAD = new ArrayList<>();
    
    public static MatrixStack.Entry lastWorldSpaceMatrix = new MatrixStack().peek();
    
    public static void onWorldRender() {
        if (!TEXTURE.isEmpty()) {
            Set<Identifier> identifiers = TEXTURE.stream()
                    .map(texture -> texture.id)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
            
            identifiers.forEach(id -> {
                RenderSystem.setShaderTexture(0, id);
                RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
                BufferBuilder buffer = Tessellator.getInstance().begin(
                        VertexFormat.DrawMode.QUADS, 
                        VertexFormats.POSITION_TEXTURE_COLOR
                );
                TEXTURE.stream()
                        .filter(texture -> texture.id.equals(id))
                        .forEach(tex -> quadTexture(tex.entry, buffer, tex.x, tex.y, tex.width, tex.height, tex.color));
                BufferRenderer.drawWithGlobalProgram(buffer.end());
            });
            
            RenderSystem.disableBlend();
            TEXTURE.clear();
        }
        
        if (!TEXTURE_DEPTH.isEmpty()) {
            Set<Identifier> identifiers = TEXTURE_DEPTH.stream()
                    .map(texture -> texture.id)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
            
            identifiers.forEach(id -> {
                RenderSystem.setShaderTexture(0, id);
                RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
                BufferBuilder buffer = Tessellator.getInstance().begin(
                        VertexFormat.DrawMode.QUADS, 
                        VertexFormats.POSITION_TEXTURE_COLOR
                );
                TEXTURE_DEPTH.stream()
                        .filter(texture -> texture.id.equals(id))
                        .forEach(tex -> quadTexture(tex.entry, buffer, tex.x, tex.y, tex.width, tex.height, tex.color));
                BufferRenderer.drawWithGlobalProgram(buffer.end());
            });
            
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            TEXTURE_DEPTH.clear();
        }
        
        if (!LINE.isEmpty()) {
            GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
            Set<Float> widths = LINE.stream().map(line -> line.width).collect(Collectors.toCollection(LinkedHashSet::new));
            
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
            
            widths.forEach(width -> {
                RenderSystem.lineWidth(width);
                BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
                LINE.stream().filter(line -> line.width == width).forEach(line -> 
                    vertexLine(line.entry, buffer, line.start.toVector3f(), line.end.toVector3f(), line.colorStart, line.colorEnd)
                );
                BufferRenderer.drawWithGlobalProgram(buffer.end());
            });
            
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            LINE.clear();
            GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
        }
        
        if (!QUAD.isEmpty()) {
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            QUAD.forEach(quad -> vertexQuad(quad.entry, buffer, quad.x, quad.y, quad.w, quad.z, quad.color));
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            QUAD.clear();
        }
        
        if (!LINE_DEPTH.isEmpty()) {
            GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
            Set<Float> widths = LINE_DEPTH.stream().map(line -> line.width).collect(Collectors.toCollection(LinkedHashSet::new));
            
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
            
            widths.forEach(width -> {
                RenderSystem.lineWidth(width);
                BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
                LINE_DEPTH.stream().filter(line -> line.width == width).forEach(line -> 
                    vertexLine(line.entry, buffer, line.start.toVector3f(), line.end.toVector3f(), line.colorStart, line.colorEnd)
                );
                BufferRenderer.drawWithGlobalProgram(buffer.end());
            });
            
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            LINE_DEPTH.clear();
            GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
        }
        
        if (!QUAD_DEPTH.isEmpty()) {
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            QUAD_DEPTH.forEach(quad -> vertexQuad(quad.entry, buffer, quad.x, quad.y, quad.w, quad.z, quad.color));
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            QUAD_DEPTH.clear();
        }
    }
    
    // Drawing methods
    public static void drawTexture(MatrixStack.Entry entry, Identifier id, float x, float y, float width, float height, Vector4i color, boolean depth) {
        Texture texture = new Texture(entry, id, x, y, width, height, color);
        if (depth) {
            TEXTURE_DEPTH.add(texture);
        } else {
            TEXTURE.add(texture);
        }
    }
    
    public static void drawLine(Vec3d start, Vec3d end, int color, float width, boolean depth) {
        drawLine(null, start, end, color, color, width, depth);
    }
    
    public static void drawLine(MatrixStack.Entry entry, Vec3d start, Vec3d end, int colorStart, int colorEnd, float width, boolean depth) {
        Line line = new Line(entry, start, end, colorStart, colorEnd, width);
        if (depth) {
            LINE_DEPTH.add(line);
        } else {
            LINE.add(line);
        }
    }
    
    public static void drawQuad(Vec3d x, Vec3d y, Vec3d w, Vec3d z, int color, boolean depth) {
        drawQuad(null, x, y, w, z, color, depth);
    }
    
    public static void drawQuad(MatrixStack.Entry entry, Vec3d x, Vec3d y, Vec3d w, Vec3d z, int color, boolean depth) {
        Quad quad = new Quad(entry, x, y, w, z, color);
        if (depth) {
            QUAD_DEPTH.add(quad);
        } else {
            QUAD.add(quad);
        }
    }
    
    public static void drawBox(Box box, int color, float width, boolean line, boolean fill, boolean depth) {
        drawBox(null, box, color, width, line, fill, depth);
    }
    
    public static void drawBox(MatrixStack.Entry entry, Box box, int color, float width, boolean line, boolean fill, boolean depth) {
        box = box.expand(1e-3);
        double x1 = box.minX;
        double y1 = box.minY;
        double z1 = box.minZ;
        double x2 = box.maxX;
        double y2 = box.maxY;
        double z2 = box.maxZ;
        
        if (fill) {
            // Extract alpha and reduce it for fill
            int alpha = (color >> 24) & 0xFF;
            int fillAlpha = (int)(alpha * 0.1f);
            int fillColor = (color & 0x00FFFFFF) | (fillAlpha << 24);
            
            drawQuad(entry, new Vec3d(x1, y1, z1), new Vec3d(x2, y1, z1), new Vec3d(x2, y1, z2), new Vec3d(x1, y1, z2), fillColor, depth);
            drawQuad(entry, new Vec3d(x1, y1, z1), new Vec3d(x1, y2, z1), new Vec3d(x2, y2, z1), new Vec3d(x2, y1, z1), fillColor, depth);
            drawQuad(entry, new Vec3d(x2, y1, z1), new Vec3d(x2, y2, z1), new Vec3d(x2, y2, z2), new Vec3d(x2, y1, z2), fillColor, depth);
            drawQuad(entry, new Vec3d(x1, y1, z2), new Vec3d(x2, y1, z2), new Vec3d(x2, y2, z2), new Vec3d(x1, y2, z2), fillColor, depth);
            drawQuad(entry, new Vec3d(x1, y1, z1), new Vec3d(x1, y1, z2), new Vec3d(x1, y2, z2), new Vec3d(x1, y2, z1), fillColor, depth);
            drawQuad(entry, new Vec3d(x1, y2, z1), new Vec3d(x1, y2, z2), new Vec3d(x2, y2, z2), new Vec3d(x2, y2, z1), fillColor, depth);
        }
        
        if (line) {
            drawLine(entry, new Vec3d(x1, y1, z1), new Vec3d(x2, y1, z1), color, color, width, depth);
            drawLine(entry, new Vec3d(x2, y1, z1), new Vec3d(x2, y1, z2), color, color, width, depth);
            drawLine(entry, new Vec3d(x2, y1, z2), new Vec3d(x1, y1, z2), color, color, width, depth);
            drawLine(entry, new Vec3d(x1, y1, z2), new Vec3d(x1, y1, z1), color, color, width, depth);
            drawLine(entry, new Vec3d(x1, y1, z2), new Vec3d(x1, y2, z2), color, color, width, depth);
            drawLine(entry, new Vec3d(x1, y1, z1), new Vec3d(x1, y2, z1), color, color, width, depth);
            drawLine(entry, new Vec3d(x2, y1, z2), new Vec3d(x2, y2, z2), color, color, width, depth);
            drawLine(entry, new Vec3d(x2, y1, z1), new Vec3d(x2, y2, z1), color, color, width, depth);
            drawLine(entry, new Vec3d(x1, y2, z1), new Vec3d(x2, y2, z1), color, color, width, depth);
            drawLine(entry, new Vec3d(x2, y2, z1), new Vec3d(x2, y2, z2), color, color, width, depth);
            drawLine(entry, new Vec3d(x2, y2, z2), new Vec3d(x1, y2, z2), color, color, width, depth);
            drawLine(entry, new Vec3d(x1, y2, z2), new Vec3d(x1, y2, z1), color, color, width, depth);
        }
    }
    
    // Vertex methods
    private static void vertexLine(MatrixStack.Entry entry, VertexConsumer buffer, Vector3f start, Vector3f end, int startColor, int endColor) {
        if (entry == null) entry = lastWorldSpaceMatrix;
        Vector3f vec = getNormal(start, end);
        buffer.vertex(entry.getPositionMatrix(), start.x, start.y, start.z).color(startColor).normal(entry, vec);
        buffer.vertex(entry.getPositionMatrix(), end.x, end.y, end.z).color(endColor).normal(entry, vec);
    }
    
    private static void vertexQuad(MatrixStack.Entry entry, VertexConsumer buffer, Vec3d vec1, Vec3d vec2, Vec3d vec3, Vec3d vec4, int color) {
        if (entry == null) entry = lastWorldSpaceMatrix;
        buffer.vertex(entry.getPositionMatrix(), (float)vec1.x, (float)vec1.y, (float)vec1.z).color(color);
        buffer.vertex(entry.getPositionMatrix(), (float)vec2.x, (float)vec2.y, (float)vec2.z).color(color);
        buffer.vertex(entry.getPositionMatrix(), (float)vec3.x, (float)vec3.y, (float)vec3.z).color(color);
        buffer.vertex(entry.getPositionMatrix(), (float)vec4.x, (float)vec4.y, (float)vec4.z).color(color);
    }
    
    private static void quadTexture(MatrixStack.Entry entry, BufferBuilder buffer, float x, float y, float width, float height, Vector4i color) {
        Matrix4f matrix = entry.getPositionMatrix();
        buffer.vertex(matrix, x, y + height, 0).texture(0, 1).color(color.x);
        buffer.vertex(matrix, x + width, y + height, 0).texture(1, 1).color(color.y);
        buffer.vertex(matrix, x + width, y, 0).texture(1, 0).color(color.w);
        buffer.vertex(matrix, x, y, 0).texture(0, 0).color(color.z);
    }
    
    // Helper methods
    private static Vector3f getNormal(Vector3f start, Vector3f end) {
        Vector3f normal = new Vector3f(start).sub(end);
        float length = (float) Math.sqrt(normal.lengthSquared());
        return normal.div(length);
    }
    
    private static int setAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }
    
    private static int getAlpha(int color) {
        return (color >> 24) & 0xFF;
    }
    
    // Records
    public record Texture(MatrixStack.Entry entry, Identifier id, float x, float y, float width, float height, Vector4i color) {}
    public record Line(MatrixStack.Entry entry, Vec3d start, Vec3d end, int colorStart, int colorEnd, float width) {}
    public record Quad(MatrixStack.Entry entry, Vec3d x, Vec3d y, Vec3d w, Vec3d z, int color) {}
}
