package itz.silentcore.feature.module.impl.misc;

import itz.silentcore.discord.DiscordIntegration;
import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.utils.client.ClientUtility;
import org.lwjgl.glfw.GLFW;

@ModuleAnnotation(name = "Discord RPC", category = Category.MISC, description = "Discord RPC integration")
public class DiscordRPC extends Module {

    public DiscordRPC() {
        super();
        setState(false);
        this.setKey(GLFW.GLFW_KEY_F3);
    }

    @Override
    public void onEnable() {
        try {
            if (DiscordIntegration.getManager() != null) {
                DiscordIntegration.getManager().initializeRPC();
                ClientUtility.sendMessage("Discord RPC enabled");
            }
        } catch (Exception e) {
            ClientUtility.sendMessage("Failed to enable Discord RPC");
        }
    }

    @Override
    public void onDisable() {
        try {
            DiscordIntegration.shutdown();
            ClientUtility.sendMessage("Discord RPC disabled");
        } catch (Exception e) {
            ClientUtility.sendMessage("Failed to disable Discord RPC");
        }
    }
}
