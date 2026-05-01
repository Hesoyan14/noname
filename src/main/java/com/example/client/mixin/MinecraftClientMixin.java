package com.example.client.mixin;

import com.example.client.feature.event.impl.TickEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickPre(CallbackInfo ci) {
        new TickEvent(true).hook();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickPost(CallbackInfo ci) {
        new TickEvent(false).hook();
    }
}
