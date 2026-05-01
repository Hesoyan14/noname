package itz.silentcore.mixin;

import itz.silentcore.SilentCore;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import itz.silentcore.feature.event.impl.ScoreboardRenderEvent;
import itz.silentcore.utils.render.RenderContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class ScoreboardMixin {

    @Inject(method = "renderScoreboardSidebar", at = @At("HEAD"), cancellable = true)
    private void onRenderScoreboard(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) {
                return;
            }

            Scoreboard scoreboard = mc.player.getScoreboard();
            ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);

            if (objective == null) {
                return;
            }

            RenderContext renderContext = new RenderContext(context);
            SilentCore.getInstance().eventBus.post(new ScoreboardRenderEvent(renderContext, objective));

            ci.cancel();
        } catch (Exception e) {
        }
    }
}
