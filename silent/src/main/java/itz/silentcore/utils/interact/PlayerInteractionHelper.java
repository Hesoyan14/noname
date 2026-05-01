package itz.silentcore.utils.interact;

import itz.silentcore.utils.client.IMinecraft;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class PlayerInteractionHelper implements IMinecraft {
    
    public static boolean isKeyPressed(int key) {
        if (mc.getWindow() == null) return false;
        
        long window = mc.getWindow().getHandle();
        
        if (key >= GLFW.GLFW_MOUSE_BUTTON_1 && key <= GLFW.GLFW_MOUSE_BUTTON_8) {
            return GLFW.glfwGetMouseButton(window, key) == GLFW.GLFW_PRESS;
        }
        return InputUtil.isKeyPressed(window, key);
    }

    public static void startFallFlying() {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        
        if (!mc.player.isGliding() && !mc.player.isOnGround()) {
            mc.getNetworkHandler().sendPacket(new net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket(mc.player, net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
    }

    public static void interactItem(Hand hand) {
        if (mc.player == null || mc.interactionManager == null) return;
        mc.interactionManager.interactItem(mc.player, hand);
    }
}
