package itz.silentcore.feature.module.impl.render;

import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.feature.module.api.setting.impl.NumberSetting;
import itz.silentcore.utils.client.IMinecraft;
import itz.silentcore.utils.render.Render2D;
import net.minecraft.util.hit.EntityHitResult;

@ModuleAnnotation(name = "CrossHair", category = Category.RENDER, description = "Custom crosshair")
public class CrossHair extends Module implements IMinecraft {
    
    private float red = 0;
    
    private final NumberSetting attackOffset = new NumberSetting("Attack Offset", 10f, 0f, 20f, 1f);
    private final NumberSetting indent = new NumberSetting("Indent", 0f, 0f, 5f, 0.5f);
    private final NumberSetting width = new NumberSetting("Width", 4f, 2f, 10f, 0.5f);
    private final NumberSetting height = new NumberSetting("Height", 1f, 1f, 4f, 0.5f);
    
    public void onRenderCrossHair() {
        if (!isEnabled() || mc.player == null) return;
        
        // Smooth interpolation for red color when targeting entity
        float targetRed = mc.crosshairTarget instanceof EntityHitResult ? 5f : 1f;
        red += (targetRed - red) * 0.5f;
        
        int firstColor = multiplyRed(0xFFFFFFFF, red);
        int secondColor = 0xFF000000;
        
        float x = mc.getWindow().getScaledWidth() / 2f;
        float y = mc.getWindow().getScaledHeight() / 2f;
        
        float cooldown = attackOffset.getCurrent() - (attackOffset.getCurrent() * mc.player.getAttackCooldownProgress(mc.getRenderTickCounter().getTickDelta(false)));
        float size = width.getCurrent();
        float size2 = height.getCurrent();
        float offset = size2 / 2;
        float indentValue = indent.getCurrent() + cooldown;
        
        renderMain(x, y, size, size2, 1, indentValue, offset, secondColor);
        renderMain(x, y, size, size2, 0, indentValue, offset, firstColor);
    }
    
    private void renderMain(float x, float y, float size, float size2, float padding, float indent, float offset, int color) {
        Render2D.drawQuad(x - offset - padding / 2, y - size - indent - padding / 2, size2 + padding, size + padding, color);
        Render2D.drawQuad(x - offset - padding / 2, y + indent - padding / 2, size2 + padding, size + padding, color);
        Render2D.drawQuad(x - size - indent - padding / 2, y - offset - padding / 2, size + padding, size2 + padding, color);
        Render2D.drawQuad(x + indent - padding / 2, y - offset - padding / 2, size + padding, size2 + padding, color);
    }
    
    private int multiplyRed(int color, float multiplier) {
        int a = (color >> 24) & 0xFF;
        int r = (int) Math.min(255, ((color >> 16) & 0xFF) * multiplier);
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
