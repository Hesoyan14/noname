package itz.silentcore.feature.module.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import itz.silentcore.feature.event.impl.WorldRenderEvent;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.feature.module.api.setting.impl.NumberSetting;
import itz.silentcore.feature.theme.ThemeManager;
import itz.silentcore.utils.client.IMinecraft;
import itz.silentcore.utils.render.ColorUtils;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

@ModuleAnnotation(name = "ChinaHat", category = Category.RENDER, description = "Renders a hat above your head")
public class ChinaHat extends Module implements IMinecraft {

    private final NumberSetting transparency = new NumberSetting("Transparency", 0.5f, 0.1f, 1.0f, 0.05f);

    @Subscribe
    public void onWorldRender(WorldRenderEvent event) {
        if (mc.player == null || mc.options.getPerspective().isFirstPerson()) {
            return;
        }

        MatrixStack stack = event.getMatrices();
        float partialTicks = event.getTickDelta();
        Vec3d playerPos = mc.player.getLerpedPos(partialTicks);
        float yOffset = (float) (playerPos.y + getYOffset(mc.player)) + 1.7f;

        stack.push();

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        stack.translate(playerPos.x, yOffset, playerPos.z);
        stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((System.currentTimeMillis() % 36000) / 100f));

        BufferBuilder cone = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        float radiusValue = 0.60f;
        float heightValue = 0.30f;
        int rgb = ThemeManager.getInstance().getPrimaryColor();
        int adjustedColor = ColorUtils.multiplyAlpha(rgb, transparency.getCurrent());

        cone.vertex(stack.peek().getPositionMatrix(), 0, heightValue, 0).color(ColorUtils.multiplyAlpha(multiplyBrightness(rgb, 0.86f), transparency.getCurrent()));

        float steps = 64;
        double angleStep = 2 * Math.PI / steps;
        for (int i = 0; i <= steps; i++) {
            float x = (float) (Math.cos(i * angleStep) * radiusValue);
            float z = (float) (Math.sin(i * angleStep) * radiusValue);
            cone.vertex(stack.peek().getPositionMatrix(), x, 0, z).color(adjustedColor);
        }

        BufferRenderer.drawWithGlobalProgram(cone.end());

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthFunc(GL11.GL_LESS);
        stack.pop();
    }

    private float getYOffset(Entity entity) {
        float offset = 0.15f;
        if (entity instanceof LivingEntity livingEntity) {
            if (livingEntity.getEquippedStack(net.minecraft.entity.EquipmentSlot.HEAD).getItem() instanceof ArmorItem) {
                offset -= 0.065f;
            }
        }
        return offset;
    }
    
    private int multiplyBrightness(int color, float multiplier) {
        int r = (int) Math.min(255, ColorUtils.getRed(color) * multiplier);
        int g = (int) Math.min(255, ColorUtils.getGreen(color) * multiplier);
        int b = (int) Math.min(255, ColorUtils.getBlue(color) * multiplier);
        int a = ColorUtils.getAlpha(color);
        return ColorUtils.rgba(r, g, b, a);
    }
}
