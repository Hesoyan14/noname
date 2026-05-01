package itz.silentcore.utils.render.providers;

public final class ColorProvider {
	
	public static int[] unpack(int color) {
		return new int[] {color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >> 24 & 0xFF};
	}

	public static float[] normalize(int color) {
		int[] components = unpack(color);
		return new float[] {components[0] / 255.0f, components[1] / 255.0f, components[2] / 255.0f, components[3] / 255.0f};
	}
}