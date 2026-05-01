package itz.silentcore.manager;

import com.google.gson.JsonObject;
import itz.silentcore.utils.client.ClientUtility;
import itz.silentcore.utils.client.Constants;
import itz.silentcore.utils.client.Log;
import itz.silentcore.web.server.ServerConnection;
import net.minecraft.client.MinecraftClient;
import ru.kotopushka.compiler.sdk.classes.Profile;

public class PlayerStatusUpdater {
    private static final PlayerStatusUpdater INSTANCE = new PlayerStatusUpdater();
    private static final ServerConnection connection = ServerConnection.getInstance();
    private static final long UPDATE_INTERVAL = 5000; 
    private long lastUpdate = 0;
    private String lastServerIp = null;
    private String lastServerName = null;

    private PlayerStatusUpdater() {
        startUpdater();
    }

    public static PlayerStatusUpdater getInstance() {
        return INSTANCE;
    }

    private void startUpdater() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(UPDATE_INTERVAL);
                    updatePlayerStatus();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    Log.error("Status update error: " + e.getMessage());
                }
            }
        }, "PlayerStatusUpdater").setDaemon(true);
    }

    private void updatePlayerStatus() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();

            
            String serverIp = "";
            String serverName = "";

            if (client.getNetworkHandler() != null && client.getNetworkHandler().getConnection() != null) {
                try {
                    var address = client.getNetworkHandler().getConnection().getAddress();
                    if (address != null) {
                        serverIp = address.toString();
                        serverName = client.getCurrentServerEntry() != null ?
                                client.getCurrentServerEntry().name : "Unknown";
                    }
                } catch (Exception e) {
                    
                }
            }

            
            if (lastServerIp != null && lastServerIp.equals(serverIp) && lastServerName != null && lastServerName.equals(serverName)) {
                return;
            }

            lastServerIp = serverIp;
            lastServerName = serverName;

        } catch (Exception e) {
            
        }
    }
}
