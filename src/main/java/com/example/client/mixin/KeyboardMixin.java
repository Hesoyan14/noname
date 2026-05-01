package com.example.client.mixin;

import com.example.client.feature.event.impl.KeyEvent;
import com.example.client.feature.ui.screen.gui.ClientGui;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS) {
            new KeyEvent(key, action).hook();

            // Right Shift открывает GUI
            if (key == GLFW.GLFW_KEY_RIGHT_SHIFT && MinecraftClient.getInstance().currentScreen == null) {
                MinecraftClient.getInstance().setScreen(new ClientGui());
            }
        }
    }
}
