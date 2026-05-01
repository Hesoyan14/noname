package itz.silentcore.manager;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class AccountManager {

    @Getter
    private String customUsername = "";
    @Getter
    private List<String> usernameHistory = new ArrayList<>();
    private final File accountFile;

    public AccountManager() {
        String os = System.getProperty("os.name").toLowerCase();
        File silentcoreFolder;

        if (os.contains("win")) {
            silentcoreFolder = new File("C:\\Silent");
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            silentcoreFolder = new File(System.getProperty("user.home") + File.separator + "SilentCore");
        } else if (os.contains("mac")) {
            silentcoreFolder = new File(System.getProperty("user.home") + File.separator + "SilentCore");
        } else {
            silentcoreFolder = new File(System.getProperty("user.home") + File.separator + "SilentCore");
        }

        if (!silentcoreFolder.exists()) {
            silentcoreFolder.mkdirs();
        }

        this.accountFile = new File(silentcoreFolder, "account.json");
        loadAccount();
    }

    public void setCustomUsername(String username) {
        if (username != null && !username.isEmpty()) {
            this.customUsername = username;

            
            if (!usernameHistory.contains(username)) {
                usernameHistory.add(0, username);
            } else {
                
                usernameHistory.remove(username);
                usernameHistory.add(0, username);
            }

            
            while (usernameHistory.size() > 10) {
                usernameHistory.remove(usernameHistory.size() - 1);
            }

            saveAccount();

            
            applySession(username);
        }
    }

    public void applySession(String username) {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null) return;

            
            UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));

            
            Session newSession = new Session(
                    username,
                    uuid,
                    "offline_token",
                    Optional.empty(),
                    Optional.empty(),
                    Session.AccountType.LEGACY
            );

            
            java.lang.reflect.Field sessionField = MinecraftClient.class.getDeclaredField("session");
            sessionField.setAccessible(true);
            sessionField.set(mc, newSession);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveAccount() {
        try {
            JsonObject accountData = new JsonObject();
            accountData.addProperty("customUsername", customUsername);

            JsonArray historyArray = new JsonArray();
            for (String username : usernameHistory) {
                historyArray.add(username);
            }
            accountData.add("usernameHistory", historyArray);

            Files.write(
                    Paths.get(accountFile.getAbsolutePath()),
                    accountData.toString().getBytes(StandardCharsets.UTF_8)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadAccount() {
        try {
            if (!accountFile.exists()) {
                customUsername = "";
                usernameHistory = new ArrayList<>();
                generateRandomUsername();
                return;
            }

            String content = new String(Files.readAllBytes(Paths.get(accountFile.getAbsolutePath())), StandardCharsets.UTF_8);
            if (content.isEmpty()) {
                customUsername = "";
                usernameHistory = new ArrayList<>();
                generateRandomUsername();
                return;
            }

            JsonObject accountData = com.google.gson.JsonParser.parseString(content).getAsJsonObject();

            if (accountData.has("customUsername")) {
                customUsername = accountData.get("customUsername").getAsString();

                if (!customUsername.isEmpty()) {
                    applySession(customUsername);
                }
            }

            if (accountData.has("usernameHistory")) {
                usernameHistory = new ArrayList<>();
                JsonArray historyArray = accountData.getAsJsonArray("usernameHistory");
                for (JsonElement element : historyArray) {
                    usernameHistory.add(element.getAsString());
                }
            }

            // Если после загрузки никнейм пустой, генерируем случайный
            if (customUsername.isEmpty()) {
                generateRandomUsername();
            }
        } catch (IOException e) {
            e.printStackTrace();
            customUsername = "";
            usernameHistory = new ArrayList<>();
            generateRandomUsername();
        }
    }

    public boolean hasCustomUsername() {
        return customUsername != null && !customUsername.isEmpty();
    }

    public String generateRandomUsername() {
        String prefix = "silentcore_";
        String randomPart = generateRandomString(10);
        String username = prefix + randomPart;
        setCustomUsername(username);
        return username;
    }

    private String generateRandomString(int length) {
        String chars = "0123456789abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
