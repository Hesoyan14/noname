package itz.silentcore.utils.client;

import itz.silentcore.SilentCore;
import itz.silentcore.manager.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;

public interface IMinecraft {
    ModuleManager moduleManager = SilentCore.getInstance().moduleManager;
    MinecraftClient mc = MinecraftClient.getInstance();
    SilentCore silentcore = SilentCore.getInstance();
    Window mw = mc.getWindow();
    RenderTickCounter tickCounter = mc.getRenderTickCounter();
}