package com.example.client.utils.render.providers;

public final class ColorProvider {

    public static int[] unpack(int color) {
        return new int[]{color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >> 24 & 0xFF};
    }

    public static float[] normalize(int color) {
        int[] c = unpack(color);
        return new float[]{c[0] / 255.0f, c[1] / 255.0f, c[2] / 255.0f, c[3] / 255.0f};
    }
}
