package com.example.client.utils.render;

import com.example.client.utils.render.draw.*;
import com.example.client.utils.render.text.Font;
import net.minecraft.client.gui.DrawContext;

public class RenderContext {
    private DrawContext context;
    private double mouseX, mouseY;

    public RenderContext(DrawContext context) {
        this.context = context;
    }

    public DrawContext getContext() { return context; }
    public void setContext(DrawContext ctx) { this.context = ctx; }
    public double getMouseX() { return mouseX; }
    public double getMouseY() { return mouseY; }
    public void setMouseX(double x) { this.mouseX = x; }
    public void setMouseY(double y) { this.mouseY = y; }

    // --- Rect ---
    public void drawRect(float x, float y, float width, float height, float radius, ColorRGBA color) {
        RectangleRenderer.draw(
                context.getMatrices().peek().getPositionMatrix(),
                x, y, width, height, radius, color.getRGB(), 0.6f);
    }

    public void drawRect(float x, float y, float width, float height,
                         float rTL, float rBL, float rBR, float rTR, ColorRGBA color) {
        RectangleRenderer.draw(
                context.getMatrices().peek().getPositionMatrix(),
                x, y, 0f, width, height,
                rTL, rBL, rBR, rTR,
                color.getRGB(), color.getRGB(), color.getRGB(), color.getRGB(), 0.6f);
    }

    public void drawRect(float x, float y, float width, float height, float radius,
                         ColorRGBA c1, ColorRGBA c2, ColorRGBA c3, ColorRGBA c4) {
        RectangleRenderer.draw(
                context.getMatrices().peek().getPositionMatrix(),
                x, y, 0f, width, height,
                radius, radius, radius, radius,
                c1.getRGB(), c2.getRGB(), c3.getRGB(), c4.getRGB(), 0.8f);
    }

    // --- Border ---
    public void drawBorder(float x, float y, float width, float height,
                           float radius, float thickness, ColorRGBA color) {
        BorderRenderer.draw(
                context.getMatrices().peek().getPositionMatrix(),
                x, y, width, height, radius, color.getRGB(), thickness, 0.4f, 0.4f);
    }

    public void drawBorder(float x, float y, float width, float height, float radius,
                           float thickness, ColorRGBA c1, ColorRGBA c2, ColorRGBA c3, ColorRGBA c4) {
        BorderRenderer.draw(
                context.getMatrices().peek().getPositionMatrix(),
                x, y, 0f, width, height,
                radius, radius, radius, radius,
                c1.getRGB(), c2.getRGB(), c3.getRGB(), c4.getRGB(),
                thickness, 0.8f, 0.8f);
    }

    // --- Blur ---
    public void drawBlur(float x, float y, float width, float height,
                         float radius, float blur, ColorRGBA color) {
        BlurRenderer.draw(
                context.getMatrices().peek().getPositionMatrix(),
                x, y, width, height, radius,
                color.getRGB(), color.getRGB(), color.getRGB(), color.getRGB(),
                blur, 0.8f);
    }

    // --- Text ---
    public void drawText(String text, Font font, float x, float y, float size, ColorRGBA color) {
        TextRenderer.draw(
                context.getMatrices().peek().getPositionMatrix(),
                x, y, font, text, size, 0.01f, 0.6f, 0.0f, color.getRGB());
    }

    public void drawText(String text, Font font, float x, float y, float size, float thickness, ColorRGBA color) {
        TextRenderer.draw(
                context.getMatrices().peek().getPositionMatrix(),
                x, y, font, text, size, thickness, 0.6f, 0.0f, color.getRGB());
    }

    // --- Styled rect (glass-like panel) ---
    public void drawStyledRect(float x, float y, float width, float height, float radius, double alpha) {
        drawRect(x, y, width, height, radius,
                ColorRGBA.of(255, 255, 255, (int) ((255 * 0.03) * alpha)));
        drawBorder(x, y, width, height, radius, 0.1f,
                ColorRGBA.of(255, 255, 255, (int) ((255 * 0.05) * alpha)));
    }
}
