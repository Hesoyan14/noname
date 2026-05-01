package itz.silentcore.discord;

import itz.silentcore.discord.utils.*;

public class DiscordExample {
    private static DiscordManager discordManager = new DiscordManager();

    public static void initialize(String applicationId) {
        discordManager.init(applicationId);
    }

    public static void updatePresence(String details, String state) {
        DiscordRichPresence richPresence = new DiscordRichPresence.Builder()
                .setStartTimestamp(System.currentTimeMillis() / 1000)
                .setDetails(details)
                .setState(state)
                .setLargeImage("https://example.com/image.png", "Large Image Text")
                .build();

        discordManager.updatePresence(richPresence);
    }

    public static void updatePresenceWithButtons(String details, String state, String button1Label, String button1Url, String button2Label, String button2Url) {
        DiscordRichPresence richPresence = new DiscordRichPresence.Builder()
                .setStartTimestamp(System.currentTimeMillis() / 1000)
                .setDetails(details)
                .setState(state)
                .setLargeImage("https://example.com/image.png", "Large Image Text")
                .setButtons(RPCButton.create(button1Label, button1Url), RPCButton.create(button2Label, button2Url))
                .build();

        discordManager.updatePresence(richPresence);
    }

    public static void shutdown() {
        discordManager.stopRPC();
    }

    public static DiscordManager getDiscordManager() {
        return discordManager;
    }
}
