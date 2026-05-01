package itz.silentcore.mixin;

import itz.silentcore.feature.event.impl.MouseClickEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Inject(at = @At("HEAD"), method = "onMouseButton")
    public void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (action != 1) {
            return;
        }
        try {
            Mouse mouse = (Mouse) (Object) this;
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.getWindow() != null) {
                Window mcWindow = mc.getWindow();
                double scaledMouseX = mouse.getX() * mcWindow.getScaledWidth() / (double) mcWindow.getWidth();
                double scaledMouseY = mouse.getY() * mcWindow.getScaledHeight() / (double) mcWindow.getHeight();
                MouseClickEvent event = new MouseClickEvent(scaledMouseX, scaledMouseY, button);
                event.hook();
            }
        } catch (Exception e) {
        }
    }
}
