package itz.silentcore.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import itz.silentcore.utils.render.ColorRGBA;
import itz.silentcore.utils.render.RenderContext;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static itz.silentcore.utils.render.Fonts.sf_pro;

@Mixin(InGameHud.class)
public class ExperienceBarMixin {

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void onRenderExperienceBar(DrawContext context, int x, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            ci.cancel();
            return;
        }

        PlayerEntity player = mc.player;
        int level = player.experienceLevel;
        String levelText = String.valueOf(level);

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        float textSize = 8f;
        float textWidth = sf_pro.getWidth(levelText, textSize);

        float centerX = screenWidth / 2f - textWidth / 2f;
        float y = screenHeight - 43f;

        RenderContext renderContext = new RenderContext(context);
        renderContext.drawText(levelText, sf_pro, centerX, y, textSize, ColorRGBA.of(255, 255, 255));

        ci.cancel();
    }
}
