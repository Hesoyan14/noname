package itz.silentcore.discord;

import itz.silentcore.discord.utils.*;

public class DiscordIntegration {
    private static final String APPLICATION_ID = "1415992048678473868";
    private static DiscordManager discordManager;

    public static void initialize() {
        // TODO: Discord RPC временно отключен для Linux
        // Раскомментируй когда добавишь библиотеку discord-rpc
        /*
        try {
            discordManager = new DiscordManager();
            discordManager.init(APPLICATION_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }

    public static void shutdown() {
        if (discordManager != null) {
            discordManager.stopRPC();
        }
    }

    public static void updateStatus(String details, String state) {
        if (discordManager != null && discordManager.isRunning()) {
            DiscordRichPresence richPresence = new DiscordRichPresence.Builder()
                    .setDetails(details)
                    .setState(state)
                    .setStartTimestamp(System.currentTimeMillis() / 1000)
                    .build();

            discordManager.updatePresence(richPresence);
        }
    }

    public static void updateStatusWithImage(String details, String state, String imageUrl, String imageText) {
        if (discordManager != null && discordManager.isRunning()) {
            DiscordRichPresence richPresence = new DiscordRichPresence.Builder()
                    .setDetails(details)
                    .setState(state)
                    .setLargeImage(imageUrl, imageText)
                    .setStartTimestamp(System.currentTimeMillis() / 1000)
                    .build();

            discordManager.updatePresence(richPresence);
        }
    }

    public static void updateStatusWithImageAndButtons(String details, String state, String imageUrl, String imageText, RPCButton... buttons) {
        if (discordManager != null && discordManager.isRunning()) {
            DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder()
                    .setDetails(details)
                    .setState(state)
                    .setLargeImage(imageUrl, imageText)
                    .setStartTimestamp(System.currentTimeMillis() / 1000);

            if (buttons.length > 0) {
                if (buttons.length == 1) {
                    builder.setButtons(buttons[0]);
                } else {
                    builder.setButtons(buttons[0], buttons[1]);
                }
            }

            discordManager.updatePresence(builder.build());
        }
    }

    public static DiscordManager getManager() {
        return discordManager;
    }

    public static boolean isActive() {
        return discordManager != null && discordManager.isRunning();
    }
}
