package com.example.client.utils.render;

public class ColorRGBA {
    private final int r, g, b, a;

    private ColorRGBA(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public static ColorRGBA of(int r, int g, int b, int a) {
        return new ColorRGBA(
            Math.max(0, Math.min(255, r)),
            Math.max(0, Math.min(255, g)),
            Math.max(0, Math.min(255, b)),
            Math.max(0, Math.min(255, a))
        );
    }

    public static ColorRGBA of(int r, int g, int b) {
        return of(r, g, b, 255);
    }

    public static ColorRGBA fromARGB(int argb) {
        return of((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >> 24) & 0xFF);
    }

    public int getR() { return r; }
    public int getG() { return g; }
    public int getB() { return b; }
    public int getA() { return a; }

    public int getRGB() {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public ColorRGBA withAlpha(int alpha) {
        return of(r, g, b, alpha);
    }

    public ColorRGBA multiply(float factor) {
        return of((int)(r * factor), (int)(g * factor), (int)(b * factor), a);
    }
}
