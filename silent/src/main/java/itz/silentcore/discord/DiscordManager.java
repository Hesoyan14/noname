package itz.silentcore.discord;

import lombok.Getter;
import lombok.Setter;
import itz.silentcore.discord.utils.*;
import itz.silentcore.utils.client.ClientUtility;
import ru.kotopushka.compiler.sdk.classes.Profile;

@Setter
@Getter
public class DiscordManager {
    private final DiscordDaemonThread discordDaemonThread = new DiscordDaemonThread();
    private boolean running = true;
    private DiscordInfo info = new DiscordInfo("Unknown", "", "");

    public void init(String applicationId) {
        DiscordNativeLoader.loadLibrary();

        if (DiscordRPC.getInstance() == null) {
            System.err.println("Discord RPC: Library not available, skipping initialization");
            this.running = false;
            return;
        }

        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().ready((user) -> {
            this.info = new DiscordInfo(user.username, "https://cdn.discordapp.com/avatars/" + user.userId + "/" + user.avatar + ".png", user.userId);
        }).build();

        DiscordRPC.getInstance().Discord_Initialize(applicationId, handlers, true, "");
        discordDaemonThread.start();
    }

    public void initializeRPC() {
        if (DiscordRPC.getInstance() == null) {
            ClientUtility.sendMessage("§7[Server] §6Discord RPC библиотека недоступна");
            return;
        }

        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("linux")) {
            ClientUtility.sendMessage("§7[Server] §6Discord RPC не поддерживается на Linux");
            return;
        }

        try {
            RPCButton telegramButton = RPCButton.create("Telegram", "https://1.com");
            RPCButton discordButton = RPCButton.create("Discord", "https://1.com");

            DiscordIntegration.updateStatusWithImageAndButtons(
                    "Роль: " + Profile.getRole(),
                    "UID: " + Profile.getUid(),
                    "https://cdn.jsdelivr.net/gh/invokestring/silentcoredlcgif@main/silentcoregifsmall.gif",
                    "t.me/silentcorenew",
                    telegramButton,
                    discordButton
            );

        } catch (Exception e) {
            ClientUtility.sendMessage("§7[Server] §cОшибка инициализации Discord RPC: " + e.getMessage());
        }
    }

    public void shutdownRPC() {
        DiscordIntegration.shutdown();
    }

    public void updatePresence(DiscordRichPresence richPresence) {
        if (DiscordRPC.getInstance() != null) {
            DiscordRPC.getInstance().Discord_UpdatePresence(richPresence);
        }
    }

    public void stopRPC() {
        if (DiscordRPC.getInstance() != null) {
            DiscordRPC.getInstance().Discord_Shutdown();
        }
        this.running = false;
    }

    private class DiscordDaemonThread extends Thread {
        @Override
        public void run() {
            this.setName("RPC");

            try {
                while (DiscordManager.this.isRunning() && DiscordRPC.getInstance() != null) {
                    DiscordRPC.getInstance().Discord_RunCallbacks();
                    Thread.sleep(15000);
                }
            } catch (Exception exception) {
                stopRPC();
            }
        }
    }

    public record DiscordInfo(String userName, String avatarUrl, String userId) {}
}
