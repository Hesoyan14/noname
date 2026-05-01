package itz.silentcore.mixin;

import itz.silentcore.SilentCore;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeFeatureRenderer.class)
public class CapeRendererMixin {
    @Inject(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/PlayerEntityRenderState;FF)V",
            at = @At("HEAD")
    )
    private void silentcore$forceCapeRender(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, PlayerEntityRenderState state, float limbAngle, float limbDistance, CallbackInfo ci) {
        SilentCore silentcore = SilentCore.getInstance();
        if (silentcore == null || !silentcore.enabled || silentcore.friendManager == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) return;

        if (state.skinTextures == null || state.skinTextures.capeTexture() == null) return;

        String targetName = state.name;
        if (targetName == null || targetName.isBlank()) return;

        boolean isSelf = false;
        if (client.player != null && client.player.getGameProfile() != null) {
            String selfName = client.player.getGameProfile().getName();
            isSelf = selfName != null && selfName.equalsIgnoreCase(targetName);
        }

        boolean shouldRender = isSelf || silentcore.friendManager.isFriend(targetName);

        if (shouldRender) {
            state.capeVisible = true;
        }
    }
}
