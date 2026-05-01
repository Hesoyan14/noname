package itz.silentcore.feature.module.impl.movement;

import com.google.common.eventbus.Subscribe;
import itz.silentcore.feature.event.impl.TickEvent;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.util.InputUtil;

@ModuleAnnotation(name = "InventoryMove", category = Category.MOVEMENT, description = "Движение в инвентаре")
public class InventoryMove extends Module {

    @Subscribe
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.currentScreen == null) return;
        
        // Не работает в чате
        if (mc.currentScreen instanceof ChatScreen) return;
        
        // Работает только в инвентарях
        if (!(mc.currentScreen instanceof HandledScreen)) return;

        updateMovementKeys();
    }

    private void updateMovementKeys() {
        long handle = mc.getWindow().getHandle();
        
        mc.options.forwardKey.setPressed(
            InputUtil.isKeyPressed(handle, mc.options.forwardKey.getDefaultKey().getCode())
        );
        mc.options.backKey.setPressed(
            InputUtil.isKeyPressed(handle, mc.options.backKey.getDefaultKey().getCode())
        );
        mc.options.leftKey.setPressed(
            InputUtil.isKeyPressed(handle, mc.options.leftKey.getDefaultKey().getCode())
        );
        mc.options.rightKey.setPressed(
            InputUtil.isKeyPressed(handle, mc.options.rightKey.getDefaultKey().getCode())
        );
        mc.options.jumpKey.setPressed(
            InputUtil.isKeyPressed(handle, mc.options.jumpKey.getDefaultKey().getCode())
        );
        mc.options.sprintKey.setPressed(
            InputUtil.isKeyPressed(handle, mc.options.sprintKey.getDefaultKey().getCode())
        );
    }
}
