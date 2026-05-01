package itz.silentcore.utils.render;

import java.awt.*;

public class ColorUtils {
    
    public static int setAlpha(int color, int alpha) {
        Color c = new Color(color, true);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, alpha))).getRGB();
    }
    
    public static int interpolateColor(int color1, int color2, float fraction) {
        fraction = Math.max(0, Math.min(1, fraction));
        
        Color c1 = new Color(color1, true);
        Color c2 = new Color(color2, true);
        
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * fraction);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * fraction);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * fraction);
        int a = (int) (c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * fraction);
        
        return new Color(r, g, b, a).getRGB();
    }
    
    public static int multiplyRed(int color, float multiplier) {
        Color c = new Color(color, true);
        int r = (int) Math.min(255, c.getRed() * multiplier);
        return new Color(r, c.getGreen(), c.getBlue(), c.getAlpha()).getRGB();
    }
    
    public static int multiplyAlpha(int color, float multiplier) {
        Color c = new Color(color, true);
        int a = (int) Math.min(255, c.getAlpha() * multiplier);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a).getRGB();
    }
    
    public static int getRed(int color) {
        return (color >> 16) & 0xFF;
    }
    
    public static int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }
    
    public static int getBlue(int color) {
        return color & 0xFF;
    }
    
    public static int getAlpha(int color) {
        return (color >> 24) & 0xFF;
    }
    
    public static int rgba(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
}
