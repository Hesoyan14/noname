package itz.silentcore.feature.module.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import itz.silentcore.feature.event.impl.Render2DEvent;
import itz.silentcore.feature.event.impl.TickEvent;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.feature.module.api.setting.impl.NumberSetting;
import itz.silentcore.feature.theme.ThemeManager;
import itz.silentcore.utils.animation.Animation;
import itz.silentcore.utils.animation.Easing;
import itz.silentcore.utils.client.IMinecraft;
import itz.silentcore.utils.friend.FriendUtils;
import itz.silentcore.utils.math.Calculate;
import itz.silentcore.utils.render.ColorUtils;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import java.util.List;

import static net.minecraft.client.render.VertexFormat.DrawMode.QUADS;
import static net.minecraft.client.render.VertexFormats.POSITION_TEXTURE_COLOR;

@ModuleAnnotation(name = "Arrows", category = Category.RENDER, description = "Shows arrows pointing to players")
public class Arrows extends Module implements IMinecraft {
    
    private final Animation radiusAnim = new Animation(150, Easing.CUBIC_OUT);
    
    private final NumberSetting radius = new NumberSetting("Radius", 50f, 30f, 100f, 1f);
    private final NumberSetting size = new NumberSetting("Size", 10f, 8f, 20f, 1f);
    
    public Arrows() {
        radiusAnim.reset(6f);
    }
    
    @Subscribe
    public void onTick(TickEvent event) {
        if (mc.player != null) {
            radiusAnim.setTarget(mc.player.isSprinting() ? 6f : 0f);
            radiusAnim.update();
        }
    }
    
    @Subscribe
    public void onRender2D(Render2DEvent event) {
        if (mc.world == null || mc.player == null) return;
        
        List<AbstractClientPlayerEntity> players = mc.world.getPlayers().stream()
                .filter(p -> p != mc.player && p.isAlive() && p.getHealth() > 0)
                .filter(p -> !isGhostPlayer(p))
                .toList();
        
        if (players.isEmpty()) return;
        
        float middleW = mc.getWindow().getScaledWidth() / 2f;
        float middleH = mc.getWindow().getScaledHeight() / 2f;
        float posY = middleH - radius.getCurrent() - radiusAnim.getValue();
        float arrowSize = size.getCurrent();
        
        if (!mc.options.hudHidden && mc.options.getPerspective().equals(Perspective.FIRST_PERSON)) {
            DrawContext drawContext = event.getContext().getContext();
            MatrixStack matrices = drawContext.getMatrices();
            
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_CONSTANT_ALPHA);
            RenderSystem.setShaderTexture(0, Identifier.of("silentcore:textures/features/arrows/arrow.png"));
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            BufferBuilder buffer = net.minecraft.client.render.Tessellator.getInstance().begin(QUADS, POSITION_TEXTURE_COLOR);
            
            for (AbstractClientPlayerEntity player : players) {
                int color = FriendUtils.isFriend(player) ? 
                    ColorUtils.rgba(0, 255, 0, 255) : 
                    ThemeManager.getInstance().getPrimaryColor();
                    
                float yaw = getRotations(player) - mc.player.getYaw();
                
                matrices.push();
                matrices.translate(middleW, middleH, 0.0F);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(yaw));
                matrices.translate(-middleW, -middleH, 0.0F);
                
                Matrix4f matrix4f = matrices.peek().getPositionMatrix();
                
                int darkColor = ColorUtils.setAlpha(multiplyColor(color, 0.4f), 128);
                
                buffer.vertex(matrix4f, middleW - (arrowSize / 2f), posY + arrowSize, 0).texture(0f, 1f).color(darkColor);
                buffer.vertex(matrix4f, middleW + arrowSize / 2f, posY + arrowSize, 0).texture(1f, 1f).color(darkColor);
                buffer.vertex(matrix4f, middleW + arrowSize / 2f, posY, 0).texture(1f, 0).color(color);
                buffer.vertex(matrix4f, middleW - (arrowSize / 2f), posY, 0).texture(0, 0).color(color);
                
                matrices.translate(middleW, middleH, 0.0F);
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-yaw));
                matrices.translate(-middleW, -middleH, 0.0F);
                matrices.pop();
            }
            
            BufferRenderer.drawWithGlobalProgram(buffer.end());
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }
    }
    
    private boolean isGhostPlayer(AbstractClientPlayerEntity player) {
        if (player.getCustomName() != null) {
            String name = player.getCustomName().getString();
            return name != null && name.startsWith("Ghost_");
        }
        return player.getClass().getSimpleName().equals("OtherClientPlayerEntity")
                && player.getPitch() == -30.0f;
    }
    
    private float getRotations(Entity entity) {
        double x = entity.getX() - mc.player.getX();
        double z = entity.getZ() - mc.player.getZ();
        return (float) -(Math.atan2(x, z) * (180 / Math.PI));
    }
    
    private int multiplyColor(int color, float multiplier) {
        int r = (int) (ColorUtils.getRed(color) * multiplier);
        int g = (int) (ColorUtils.getGreen(color) * multiplier);
        int b = (int) (ColorUtils.getBlue(color) * multiplier);
        int a = ColorUtils.getAlpha(color);
        return ColorUtils.rgba(r, g, b, a);
    }
}
