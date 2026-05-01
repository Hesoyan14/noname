package itz.silentcore.mixin;

import itz.silentcore.utils.animation.Animation;
import itz.silentcore.utils.animation.Easing;
import itz.silentcore.utils.render.RenderContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

    @Unique
    private final Animation tabAnimation = new Animation(250, Easing.BAKEK_SIZE);

    @Inject(method = "setVisible", at = @At("HEAD"))
    private void onVisibilityChange(boolean visible, CallbackInfo ci) {
        if (!visible) {
            tabAnimation.animate(0);
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRenderStart(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective, CallbackInfo ci) {
        tabAnimation.update();
        tabAnimation.animate(1);

        float delta = tabAnimation.getValue();

        if (delta == 0) {
            ci.cancel();
            return;
        }

        MatrixStack matrices = context.getMatrices();
        matrices.push();

        float centerX = scaledWindowWidth / 2.0f;
        float centerY = 10.0f;

        float scale = Math.max(0.0001f, delta);
        matrices.translate(centerX, centerY, 0.0f);
        matrices.scale(scale, scale, 1.0f);
        matrices.translate(-centerX, -centerY, 0.0f);

        RenderContext.setTabOrScoreboard(true);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderEnd(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective, CallbackInfo ci) {
        RenderContext.setTabOrScoreboard(false);

        float delta = tabAnimation.getValue();

        if (delta != 0) {
            context.getMatrices().pop();
        }
    }
}