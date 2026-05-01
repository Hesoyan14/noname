package itz.silentcore.utils.render;

import itz.silentcore.utils.render.draw.*;
import itz.silentcore.utils.render.text.Font;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;

import static itz.silentcore.utils.client.IMinecraft.mc;

@Setter
@Getter
public class RenderContext {
    private DrawContext context;
    private double mouseX = 0, mouseY = 0;

    private static final ThreadLocal<Boolean> IS_TAB_OR_SCOREBOARD = ThreadLocal.withInitial(() -> false);

    public static void setTabOrScoreboard(boolean value) {
        IS_TAB_OR_SCOREBOARD.set(value);
    }

    public static boolean isTabOrScoreboard() {
        return IS_TAB_OR_SCOREBOARD.get();
    }

    public static void withTabOrScoreboard(Runnable runnable) {
        boolean was = IS_TAB_OR_SCOREBOARD.get();
        IS_TAB_OR_SCOREBOARD.set(true);
        try {
            runnable.run();
        } finally {
            IS_TAB_OR_SCOREBOARD.set(was);
        }
    }

    public RenderContext(DrawContext context) {
        this.context = context;
    }

    public void drawRect(float x, float y, float width, float height, float radius, ColorRGBA color) {
        RectangleRenderer.draw(
                context.getMatrices().peek().getPositionMatrix(),
                x,
                y,
                width,
                height,
                radius,
                color.getRGB(),
                0.6f
        );
    }

    // Draw rectangle with individual corner radii: TL, BL, BR, TR
    public void drawRect(float x, float y, float width, float height,
                         float radiusTopLeft, float radiusBottomLeft,
                         float radiusBottomRight, float radiusTopRight,
                         ColorRGBA color) {
        RectangleRenderer.draw(
                context.getMatrices().peek().getPositionMatrix(),
                x,
                y,
                0.0f,
                width,
                height,
                radiusTopLeft,
                radiusBottomLeft,
                radiusBottomRight,
                radiusTopRight,
                color.getRGB(),
                color.getRGB(),
                color.getRGB(),
                color.getRGB(),
                0.6f
        );
    }

    public void drawRect(float x, float y, float width, float height, float radius, ColorRGBA color, float smoothness) {
        RectangleRenderer.draw(
                context.getMatrices().peek().getPositionMatrix(),
                x,
                y,
                width,
                height,
                radius,
                color.getRGB(),
                smoothness
        );
    }

    public void drawRect(float x, float y, float width, float height, float radius,
                         ColorRGBA color, ColorRGBA color2, ColorRGBA color3, ColorRGBA color4) {
        RectangleRenderer.draw(
                context.getMatrices().peek().getPositionMatrix(),
                x,
                y,
                0.0f,
                width,
                height,
                radius,
                radius,
                radius,
                radius,
                color.getRGB(),
                color2.getRGB(),
                color3.getRGB(),
                color4.getRGB(),
                0.8f
        );
    }

    public void drawTexture(float x, float y, float width, float height, float radius, ColorRGBA color,
                            float u, float v, float texWidth, float texHeight, int textureId) {
        TextureRenderer.draw(
                context.getMatrices().peek().getPositionMatrix(),
                x, y, width, height, radius,
                color.getRGB(), color.getRGB(), color.getRGB(), color.getRGB(),
                u, v, texWidth, texHeight, textureId,
                0.6f
        );
    }

    public void drawTexture(float x, float y, float width, float height, float radius, ColorRGBA color,
                            float u, float v, float texWidth, float texHeight, int textureId, float smoothness) {
        TextureRenderer.draw(
                context.getMatrices().peek().getPositionMatrix(),
                x, y, width, height, radius,
                color.getRGB(), color.getRGB(), color.getRGB(), color.getRGB(),
                u, v, texWidth, texHeight, textureId,
                smoothness
        );
    }

    public void drawTexture(float x, float y, float width, float height, float radius,
                            ColorRGBA color, ColorRGBA color2, ColorRGBA color3, ColorRGBA color4,
                            float u, float v, float texWidth, float texHeight, int textureId) {
        TextureRenderer.draw(
                context.getMatrices().peek().getPositionMatrix(),
                x, y, 0.0f,
                width, height,
                radius, radius, radius, radius,
                color.getRGB(), color2.getRGB(), color3.getRGB(), color4.getRGB(),
                u, v, texWidth, texHeight, textureId,
                0.8f
        );
    }

    public void drawBlur(float x, float y, float width, float height, float radius, float blur, ColorRGBA color) {
        BlurRenderer.draw(
                context.getMatrices().peek().getPositionMatrix(),
                x, y, width, height, radius,
                color.getRGB(), color.getRGB(), color.getRGB(), color.getRGB(),
                blur, 0.8f
        );
    }

    public void drawBlur(float x, float y, float width, float height, float radius, float blur, ColorRGBA color, float smoothness) {
        BlurRenderer.draw(
                context.getMatrices().peek().getPositionMatrix(),
                x, y, width, height, radius,
                color.getRGB(), color.getRGB(), color.getRGB(), color.getRGB(),
                blur, smoothness
        );
    }

    public void drawBlur(float x, float y, float width, float height, float radius, float blur,
                         ColorRGBA color, ColorRGBA color2, ColorRGBA color3, ColorRGBA color4) {
        BlurRenderer.draw(
                context.getMatrices().peek().getPositionMatrix(),
                x,
                y,
                width,
                height,
                radius,
                color.getRGB(),
                color2.getRGB(),
                color3.getRGB(),
                color4.getRGB(),
                blur,
                0.8f
        );
    }

    public void drawBorder(float x, float y, float width, float height, float radius, float thickness, ColorRGBA color) {
        BorderRenderer.draw(
                context.getMatrices().peek().getPositionMatrix(),
                x,
                y,
                width,
                height,
                radius,
                color.getRGB(),
                thickness,
                0.4f,
                0.4f
        );
    }

    public void drawBorder(float x, float y, float width, float height, float radius, float thickness, ColorRGBA color, float innersmoothness, float outersmoothnes) {
        BorderRenderer.draw(
                context.getMatrices().peek().getPositionMatrix(),
                x,
                y,
                width,
                height,
                radius,
                color.getRGB(),
                thickness,
                innersmoothness,
                outersmoothnes
        );
    }

    public void drawBorder(float x, float y, float width, float height, float radius, float thickness,
                           ColorRGBA color, ColorRGBA color2, ColorRGBA color3, ColorRGBA color4) {
        BorderRenderer.draw(
                context.getMatrices().peek().getPositionMatrix(),
                x,
                y,
                0.0f,
                width,
                height,
                radius,
                radius,
                radius,
                radius,
                color.getRGB(),
                color2.getRGB(),
                color3.getRGB(),
                color4.getRGB(),
                thickness,
                0.8f,
                0.8f
        );
    }

    public void drawText(String text, Font font, float x, float y, float size, ColorRGBA color) {
        TextRenderer.draw(
                context.getMatrices().peek().getPositionMatrix(),
                x,
                y,
                font,
                text,
                size,
                0.01f,
                0.6f,
                0.0f,
                color.getRGB()
        );
    }

    public void drawText(String text, Font font, float x, float y, float size, float thickness, ColorRGBA color) {
        TextRenderer.draw(
                context.getMatrices().peek().getPositionMatrix(),
                x,
                y,
                font,
                text,
                size,
                thickness,
                0.6f,
                0.0f,
                color.getRGB()
        );
    }

    public void drawStyledRect(float x, float y, float width, float height, float radius, double alpha) {
        drawRect(x, y, width, height, radius, ColorRGBA.of(255, 255, 255, (int) ((255 * 0.03) * alpha)));
        drawBorder(x, y, width, height, radius, 0.1f, ColorRGBA.of(255, 255, 255, (int) ((255 * 0.05) * alpha)));
    }
}
