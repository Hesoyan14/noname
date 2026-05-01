package itz.silentcore.feature.module.impl.render;

import com.google.common.eventbus.Subscribe;
import itz.silentcore.feature.event.impl.Render2DEvent;
import itz.silentcore.feature.event.impl.TickEvent;
import itz.silentcore.feature.event.impl.WorldRenderEvent;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.feature.module.api.setting.impl.BooleanSetting;
import itz.silentcore.feature.module.api.setting.impl.ModeSetting;
import itz.silentcore.feature.module.api.setting.impl.MultiBooleanSetting;
import itz.silentcore.feature.module.api.setting.impl.NumberSetting;
import itz.silentcore.feature.theme.ThemeManager;
import itz.silentcore.utils.math.Projection;
import itz.silentcore.utils.render.ColorRGBA;
import itz.silentcore.utils.render.Fonts;
import itz.silentcore.utils.render.Render3D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4d;

import java.util.ArrayList;
import java.util.List;

@ModuleAnnotation(name = "ESP", category = Category.RENDER, description = "Highlights entities")
public class ESP extends Module {

    public MultiBooleanSetting entityType = new MultiBooleanSetting("Entity Type",
        new MultiBooleanSetting.Value("Player", true),
        new MultiBooleanSetting.Value("Item", true),
        new MultiBooleanSetting.Value("TNT", false)
    );
    
    public ModeSetting boxType = new ModeSetting("Box Type", "3D Box");
    
    public NumberSetting boxAlpha = new NumberSetting("Box Alpha", 0.3f, 0.1f, 1.0f, 0.05f);
    
    public BooleanSetting wallCheck = new BooleanSetting("Wall Check", true);
    
    public MultiBooleanSetting playerInfo = new MultiBooleanSetting("Player Info",
        new MultiBooleanSetting.Value("Name", true),
        new MultiBooleanSetting.Value("Health", true),
        new MultiBooleanSetting.Value("Items", false)
    );
    
    public NumberSetting textScale = new NumberSetting("Text Scale", 1.0f, 0.5f, 2.0f, 0.1f);

    private final List<PlayerEntity> players = new ArrayList<>();

    @Subscribe
    public void onTick(TickEvent e) {
        players.clear();
        if (mc.world != null) {
            mc.world.getPlayers().stream()
                    .filter(player -> player != mc.player)
                    .filter(player -> !player.isSpectator())
                    .forEach(players::add);
        }
    }

    @Subscribe
    public void onWorldRender(WorldRenderEvent e) {
        if (!entityType.isEnable("Player")) return;
        
        float tickDelta = mc.getRenderTickCounter().getTickDelta(false);
        Camera camera = mc.gameRenderer.getCamera();
        
        for (PlayerEntity player : players) {
            if (player == null || player == mc.player) continue;
            
            double interpX = MathHelper.lerp(tickDelta, player.prevX, player.getX());
            double interpY = MathHelper.lerp(tickDelta, player.prevY, player.getY());
            double interpZ = MathHelper.lerp(tickDelta, player.prevZ, player.getZ());
            
            Vec3d interpCenter = new Vec3d(interpX, interpY, interpZ);
            float distance = (float) camera.getPos().distanceTo(interpCenter);
            
            if (distance < 1) continue;
            
            // Get box dimensions in world space
            Box box = player.getDimensions(player.getPose()).getBoxAt(interpX, interpY, interpZ);
            
            int baseColor = ThemeManager.getInstance().getPrimaryColor();
            int alpha = (int) (boxAlpha.getCurrent() * 255);
            int fillColor = (baseColor & 0x00FFFFFF) | (alpha << 24);
            int outlineColor = baseColor | 0xFF000000;

            // Draw box with world coordinates - always visible
            Render3D.drawBox(box, fillColor, 2, true, true, true);
            Render3D.drawBox(box, outlineColor, 2, true, false, true);
        }
        
        // Render items and TNT
        if (entityType.isEnable("Item") || entityType.isEnable("TNT")) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity instanceof ItemEntity item && entityType.isEnable("Item")) {
                    Box box = item.getBoundingBox();
                    
                    int baseColor = ThemeManager.getInstance().getPrimaryColor();
                    int alpha = (int) (0.3 * 255);
                    int fillColor = (baseColor & 0x00FFFFFF) | (alpha << 24);
                    int outlineColor = baseColor | 0xFF000000;
                    
                    Render3D.drawBox(box, fillColor, 2, true, true, true);
                    Render3D.drawBox(box, outlineColor, 2, true, false, true);
                } else if (entity instanceof TntEntity tnt && entityType.isEnable("TNT")) {
                    Box box = tnt.getBoundingBox();
                    
                    int color = 0xFFFF0000;
                    int alpha = (int) (0.3 * 255);
                    int fillColor = (color & 0x00FFFFFF) | (alpha << 24);
                    int outlineColor = color | 0xFF000000;
                    
                    Render3D.drawBox(box, fillColor, 2, true, true, true);
                    Render3D.drawBox(box, outlineColor, 2, true, false, true);
                }
            }
        }
    }
    
    @Subscribe
    public void onRender2D(Render2DEvent event) {
        if (!entityType.isEnable("Player")) return;
        if (mc.player == null || mc.world == null) return;
        
        DrawContext context = event.getContext().getContext();
        float tickDelta = mc.getRenderTickCounter().getTickDelta(false);
        float scale = textScale.getCurrent();
        
        for (PlayerEntity player : players) {
            if (player == null || player == mc.player) continue;
            
            try {
                // Get interpolated position
                double interpX = MathHelper.lerp(tickDelta, player.prevX, player.getX());
                double interpY = MathHelper.lerp(tickDelta, player.prevY, player.getY());
                double interpZ = MathHelper.lerp(tickDelta, player.prevZ, player.getZ());
                Vec3d interpPos = new Vec3d(interpX, interpY + player.getHeight() + 0.3, interpZ);
                
                // Get 2D screen position
                Vec3d screenPos = Projection.worldToScreen(interpPos);
                
                // Check if on screen
                if (screenPos.z < 0 || screenPos.z >= 1) continue;
                
                MatrixStack matrices = context.getMatrices();
                matrices.push();
                matrices.scale(scale, scale, 1.0f);
                
                float x = (float) (screenPos.x / scale);
                float y = (float) (screenPos.y / scale);
                
                // Build name and health text on same line
                StringBuilder nameHealthText = new StringBuilder();
                if (playerInfo.isEnable("Name")) {
                    nameHealthText.append(player.getName().getString());
                }
                if (playerInfo.isEnable("Health")) {
                    if (nameHealthText.length() > 0) {
                        nameHealthText.append(" ");
                    }
                    float health = player.getHealth();
                    nameHealthText.append(String.format("%.1f HP", health));
                }
                
                // Draw name and health on same line - all white
                if (nameHealthText.length() > 0) {
                    String text = nameHealthText.toString();
                    int textWidth = mc.textRenderer.getWidth(text);
                    
                    // Draw background
                    context.fill((int)(x - textWidth / 2f - 2), (int)(y - 2), 
                               (int)(x + textWidth / 2f + 2), (int)(y + 8), 
                               0x80000000);
                    
                    // Draw text - all white
                    context.drawText(mc.textRenderer, text, 
                                   (int)(x - textWidth / 2f), (int)y, 
                                   0xFFFFFFFF, true);
                    
                    y += 10;
                }
                
                // Draw items below name
                if (playerInfo.isEnable("Items")) {
                    int itemCount = 0;
                    for (var stack : player.getHandItems()) {
                        if (!stack.isEmpty()) itemCount++;
                    }
                    
                    if (itemCount > 0) {
                        float startX = x - (itemCount * 9);
                        for (var stack : player.getHandItems()) {
                            if (!stack.isEmpty()) {
                                context.drawItem(stack, (int)startX, (int)y);
                                startX += 18;
                            }
                        }
                    }
                }
                
                matrices.pop();
            } catch (Exception e) {
                // Skip this player if rendering fails
            }
        }
    }
    
    private int getHealthColor(float health, float maxHealth) {
        float ratio = health / maxHealth;
        if (ratio > 0.75f) return 0x00FF00; // Green
        if (ratio > 0.5f) return 0xFFFF00;  // Yellow
        if (ratio > 0.25f) return 0xFFA500; // Orange
        return 0xFF0000; // Red
    }
}
