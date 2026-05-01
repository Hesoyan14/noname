package itz.silentcore.utils.render;

import lombok.Getter;

@Getter
public class ColorRGBA {
    private final int r, g, b, a;
    public ColorRGBA(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public ColorRGBA(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public ColorRGBA(int rgba) {
        this.r = (rgba >> 16) & 0xFF;
        this.g = (rgba >> 8) & 0xFF;
        this.b = rgba & 0xFF;
        this.a = (rgba >> 24) & 0xFF;
    }

    public static ColorRGBA of(int r, int g, int b, int a) {
        return new ColorRGBA(r, g, b, a);
    }

    public static ColorRGBA of(int r, int g, int b) {
        return new ColorRGBA(r, g, b, 255);
    }

    public int getRGB() {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public ColorRGBA withAlpha(int alpha) {
        return new ColorRGBA(r, g, b, alpha);
    }

    public ColorRGBA mulAlpha(float multiplier) {
        int newAlpha = (int) (a * multiplier);
        return new ColorRGBA(r, g, b, Math.max(0, Math.min(255, newAlpha)));
    }
    
    // Методы для получения компонентов цвета как float (0.0 - 1.0)
    public static float redf(int color) {
        return ((color >> 16) & 0xFF) / 255.0f;
    }
    
    public static float greenf(int color) {
        return ((color >> 8) & 0xFF) / 255.0f;
    }
    
    public static float bluef(int color) {
        return (color & 0xFF) / 255.0f;
    }
    
    public static float alphaf(int color) {
        return ((color >> 24) & 0xFF) / 255.0f;
    }
}