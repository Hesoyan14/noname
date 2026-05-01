package itz.silentcore.mixin;

import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.ArrayList;
import java.util.List;

@Mixin(ServerList.class)
public class ServerListMixin {

    @Unique
    private static final List<ServerInfo> BABKASERVER = new ArrayList<>();

    static {
        BABKASERVER.add(createServerInfo("§9⚡ silentcore HvH", "silentcore.silentcorehvh.fun"));
        BABKASERVER.add(createServerInfo("§6⚡ Night HvH", "silentcore.nighthvh.space"));
        BABKASERVER.add(createServerInfo("§c⚡ Сервер для настройки конфига", "test.mioclient.me"));
    }

    @Shadow
    @Final
    private List<ServerInfo> servers;

    @Inject(method = "loadFile", at = @At("TAIL"))
    private void onLoadFile(CallbackInfo ci) {
        for (ServerInfo customServer : BABKASERVER) {
            if (containsServer(customServer)) {
                servers.add(customServer);
            }
        }
    }

    @Inject(method = "saveFile", at = @At("HEAD"))
    private void onSaveFile(CallbackInfo ci) {
        List<ServerInfo> toRemove = new ArrayList<>();

        for (ServerInfo server : servers) {
            for (ServerInfo customServer : BABKASERVER) {
                if (server.address != null && server.address.equals(customServer.address)) {
                    toRemove.add(server);
                    break;
                }
            }
        }

        servers.removeAll(toRemove);
    }

    @Inject(method = "saveFile", at = @At("TAIL"))
    private void afterSaveFile(CallbackInfo ci) {
        for (ServerInfo customServer : BABKASERVER) {
            if (containsServer(customServer)) {
                servers.add(customServer);
            }
        }
    }

    @Unique
    private boolean containsServer(ServerInfo serverToCheck) {
        for (ServerInfo server : servers) {
            if (server.address != null && server.address.equals(serverToCheck.address)) {
                return false;
            }
        }
        return true;
    }

    @Unique
    private static ServerInfo createServerInfo(String name, String address) {
        ServerInfo serverInfo = new ServerInfo(name, address, ServerInfo.ServerType.REALM);
        serverInfo.setResourcePackPolicy(ServerInfo.ResourcePackPolicy.PROMPT);
        return serverInfo;
    }
}