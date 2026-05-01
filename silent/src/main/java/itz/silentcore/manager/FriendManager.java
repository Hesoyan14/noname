package itz.silentcore.manager;

import com.google.gson.*;
import itz.silentcore.utils.client.ClientUtility;
import itz.silentcore.utils.client.Constants;
import itz.silentcore.utils.client.Log;
import itz.silentcore.web.server.ServerConnection;
import lombok.Getter;
import ru.kotopushka.compiler.sdk.classes.Profile;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class FriendManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final ServerConnection connection = ServerConnection.getInstance();
    private static final int AUTH_TIMEOUT_SECONDS = 10;

    private final List<String> friendsList = new CopyOnWriteArrayList<>();
    private final File friendsFile;
    private final CountDownLatch authLatch = new CountDownLatch(1);

    @Getter
    private volatile boolean isVerified = false;

    public FriendManager() {
        this.friendsFile = new File(getsilentcoreFolder(), "Friends.silentcore");
        ensureFriendsDirectory();
        loadLocalFriends();
    }

    public boolean initialize() {
        return authenticate();
    }

    private boolean authenticate() {
        new Thread(this::performAuth, "Friends-Auth").start();

        try {
            boolean success = authLatch.await(AUTH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!success) {
                Log.error("Friend system authentication timeout");
                ClientUtility.sendMessage("§cОшибка аутентификации друзей");
            }
            return success;
        } catch (InterruptedException e) {
            Log.error("Authentication interrupted: " + e.getMessage());
            return false;
        }
    }

    private void performAuth() {
        try {
            JsonObject request = new JsonObject();
            request.addProperty("action", "friends:verify");
            request.addProperty("apiKey", Constants.API_KEY);
            request.addProperty("username", Profile.getUsername());

            String response = connection.sendRequest(request);
            String decrypted = itz.silentcore.web.server.ClientCrypto.decryptFriendsResponse(response);
            JsonObject jsonResponse = JsonParser.parseString(decrypted).getAsJsonObject();

            JsonElement statusElement = jsonResponse.get("status");
            if (statusElement != null && "success".equals(statusElement.getAsString())) {
                isVerified = true;
                ClientUtility.sendMessage("§7[Friends] §aУспешная аутентификация");
                fetchFriendsFromServer();
            } else {
                Log.error("Friends verification failed");
            }
        } catch (Exception e) {
            Log.error("Friends auth error: " + e.getMessage());
        } finally {
            authLatch.countDown();
        }
    }

    private void fetchFriendsFromServer() {
        new Thread(() -> {
            try {
                JsonObject request = new JsonObject();
                request.addProperty("action", "friends:get");
                request.addProperty("apiKey", Constants.API_KEY);
                request.addProperty("username", Profile.getUsername());

                String response = connection.sendRequest(request);
                String decrypted = itz.silentcore.web.server.ClientCrypto.decryptFriendsResponse(response);
                JsonObject jsonResponse = JsonParser.parseString(decrypted).getAsJsonObject();

                JsonElement statusElement = jsonResponse.get("status");
                if (statusElement != null && "success".equals(statusElement.getAsString())) {
                    JsonElement friendsElement = jsonResponse.get("friends");
                    if (friendsElement != null) {
                        String friendsStr = friendsElement.getAsString();
                        if (!friendsStr.isEmpty()) {
                            friendsList.clear();
                            for (String friend : friendsStr.split(",")) {
                                friendsList.add(friend.trim());
                            }
                            saveLocalFriends();
                        }
                    }
                }
            } catch (Exception e) {
                Log.error("Failed to fetch friends: " + e.getMessage());
            }
        }, "Friends-Fetch").start();
    }

    public void addFriend(String friendName) {
        if (friendsList.contains(friendName)) {
            ClientUtility.sendMessage("§cДруг уже в списке");
            return;
        }

        new Thread(() -> {
            try {
                JsonObject request = new JsonObject();
                request.addProperty("action", "friends:add");
                request.addProperty("apiKey", Constants.API_KEY);
                request.addProperty("username", Profile.getUsername());
                request.addProperty("friend", friendName);

                String response = connection.sendRequest(request);
                String decrypted = itz.silentcore.web.server.ClientCrypto.decryptFriendsResponse(response);
                JsonObject jsonResponse = JsonParser.parseString(decrypted).getAsJsonObject();

                if ("success".equals(jsonResponse.get("status").getAsString())) {
                    friendsList.add(friendName);
                    saveLocalFriends();
                    ClientUtility.sendMessage("§a" + friendName + " добавлен в друзья");
                } else {
                    ClientUtility.sendMessage("§cОшибка добавления друга");
                }
            } catch (Exception e) {
                ClientUtility.sendMessage("§cОшибка: " + e.getMessage());
                Log.error("Add friend error: " + e.getMessage());
            }
        }, "Friends-Add").start();
    }

    public void removeFriend(String friendName) {
        if (!friendsList.contains(friendName)) {
            ClientUtility.sendMessage("§cДруга нет в списке");
            return;
        }

        new Thread(() -> {
            try {
                JsonObject request = new JsonObject();
                request.addProperty("action", "friends:remove");
                request.addProperty("apiKey", Constants.API_KEY);
                request.addProperty("username", Profile.getUsername());
                request.addProperty("friend", friendName);

                String response = connection.sendRequest(request);
                String decrypted = itz.silentcore.web.server.ClientCrypto.decryptFriendsResponse(response);
                JsonObject jsonResponse = JsonParser.parseString(decrypted).getAsJsonObject();

                if ("success".equals(jsonResponse.get("status").getAsString())) {
                    friendsList.remove(friendName);
                    saveLocalFriends();
                    ClientUtility.sendMessage("§a" + friendName + " удалён из друзей");
                } else {
                    ClientUtility.sendMessage("§cОшибка удаления друга");
                }
            } catch (Exception e) {
                ClientUtility.sendMessage("§cОшибка: " + e.getMessage());
                Log.error("Remove friend error: " + e.getMessage());
            }
        }, "Friends-Remove").start();
    }

    public List<String> getFriendsList() {
        return new ArrayList<>(friendsList);
    }

    public boolean isFriend(String name) {
        return friendsList.contains(name);
    }

    public void listFriends() {
        if (friendsList.isEmpty()) {
            ClientUtility.sendMessage("§cУ вас нет друзей");
            return;
        }
        ClientUtility.sendMessage("§7Ваши друзья:");
        friendsList.forEach(friend -> ClientUtility.sendMessage("§f  - " + friend));
    }

    private void loadLocalFriends() {
        if (!friendsFile.exists()) return;
        try (FileReader reader = new FileReader(friendsFile)) {
            JsonObject data = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray array = data.getAsJsonArray("friends");
            if (array != null) {
                friendsList.clear();
                array.forEach(e -> friendsList.add(e.getAsString()));
            }
        } catch (Exception e) {
            Log.error("Failed to load local friends: " + e.getMessage());
        }
    }

    private void saveLocalFriends() {
        try {
            JsonObject data = new JsonObject();
            JsonArray array = new JsonArray();
            friendsList.forEach(array::add);
            data.add("friends", array);
            data.addProperty("savedAt", System.currentTimeMillis());

            try (FileWriter writer = new FileWriter(friendsFile)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            Log.error("Failed to save local friends: " + e.getMessage());
        }
    }

    private File getsilentcoreFolder() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win") ? new File("C:\\Silent") : new File(System.getProperty("user.home"), "SilentCore");
    }

    private void ensureFriendsDirectory() {
        File dir = friendsFile.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}
