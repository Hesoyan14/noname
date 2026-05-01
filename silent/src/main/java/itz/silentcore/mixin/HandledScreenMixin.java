package itz.silentcore.mixin;

import itz.silentcore.SilentCore;
import itz.silentcore.feature.event.impl.HandledScreenEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    
    @Shadow
    public int backgroundWidth;
    
    @Shadow
    public int backgroundHeight;
    
    @Shadow
    @Nullable
    protected Slot focusedSlot;
    
    @Inject(method = "render", at = @At("RETURN"))
    public void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        HandledScreenEvent event = new HandledScreenEvent(context, focusedSlot, backgroundWidth, backgroundHeight);
        SilentCore.getInstance().eventBus.post(event);
    }
}
