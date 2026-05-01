package itz.silentcore.mixin;

import itz.silentcore.SilentCore;
import itz.silentcore.feature.event.impl.JumpEvent;
import itz.silentcore.feature.event.impl.SwingDurationEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    
    @Shadow
    public abstract boolean hasStatusEffect(RegistryEntry<StatusEffect> effect);
    
    @Shadow
    public abstract net.minecraft.entity.effect.StatusEffectInstance getStatusEffect(RegistryEntry<StatusEffect> effect);
    
    @Inject(method = "jump", at = @At("HEAD"))
    private void onJump(CallbackInfo ci) {
        if ((Object) this instanceof ClientPlayerEntity player) {
            JumpEvent event = new JumpEvent(player);
            SilentCore.getInstance().eventBus.post(event);
        }
    }
    
    @Inject(method = "getHandSwingDuration", at = @At("HEAD"), cancellable = true)
    private void onGetHandSwingDuration(CallbackInfoReturnable<Integer> cir) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if ((Object) this != mc.player) return;
        
        SwingDurationEvent event = new SwingDurationEvent();
        SilentCore.getInstance().eventBus.post(event);
        
        if (event.isCancelled()) {
            float animation = event.getAnimation();
            if (StatusEffectUtil.hasHaste(mc.player)) {
                animation *= (6 - (1 + StatusEffectUtil.getHasteAmplifier(mc.player)));
            } else {
                animation *= (hasStatusEffect(StatusEffects.MINING_FATIGUE) ? 
                    6 + (1 + getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) * 2 : 6);
            }
            cir.setReturnValue((int) animation);
        }
    }
}
