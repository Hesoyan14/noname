package itz.silentcore.mixin;

import itz.silentcore.SilentCore;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin {

    @Unique private static final Identifier CUSTOM_CAPE = Identifier.of("silentcore", "cape/cape.png");
    @Unique private static final Identifier CUSTOM_ELYTRA = Identifier.of("silentcore", "cape/elytra.png");

    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void injectGetSkinTextures(CallbackInfoReturnable<SkinTextures> cir) {
        
        SkinTextures original = cir.getReturnValue();

        AbstractClientPlayerEntity self = (AbstractClientPlayerEntity) (Object) this;

        String name = self.getGameProfile() != null ? self.getGameProfile().getName() : null;
        boolean isSelf = false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.player != null) {
            try {
                isSelf = self.getUuid().equals(mc.player.getUuid());
            } catch (Throwable ignored) {}
        }

        boolean isFriend = false;
        try {
            isFriend = name != null && SilentCore.getInstance() != null && SilentCore.getInstance().friendManager != null
                    && SilentCore.getInstance().friendManager.isFriend(name);
        } catch (Throwable ignored) {}

        boolean capeEnabled = true;
        try {
        } catch (Throwable ignored) {}

        if ((isSelf || isFriend) && capeEnabled) {
            SkinTextures newTextures = new SkinTextures(
                    original.texture(),
                    original.textureUrl(),
                    CUSTOM_CAPE,
                    CUSTOM_ELYTRA,
                    original.model(),
                    original.secure()
            );
            cir.setReturnValue(newTextures);
        }
    }
}