package itz.silentcore.web.server;

import com.google.gson.JsonObject;
import itz.silentcore.utils.client.ClientUtility;
import itz.silentcore.utils.client.Constants;
import itz.silentcore.utils.client.Log;
import net.minecraft.text.Text;
import ru.kotopushka.compiler.sdk.classes.Profile;

import java.util.regex.Pattern;

public class IRC {
    private static final ServerConnection connection = ServerConnection.getInstance();
    private static final Pattern LINK_PATTERN = Pattern.compile(
            "(?i)\\b((?:https?://|www\\.)?[a-z0-9-]+(?:\\.[a-z0-9-]+)*\\.[a-z]{2,}(?::\\d{2,5})?(?:/[^\\s]*)?)"
    );
    private static final Pattern SPAM_PATTERN = Pattern.compile(
            "(?i)\\b(\\w+)\\1{2,}\\b"
    );

    public IRC() {
        performHandshake();
    }

    private void performHandshake() {
        new Thread(() -> {
            try {
                JsonObject request = new JsonObject();
                request.addProperty("action", "irc:handshake");
                request.addProperty("apiKey", Constants.API_KEY);
                request.addProperty("username", Profile.getUsername());

                connection.sendRequest(request);
                ClientUtility.sendIRCMessage(" Подключение успешно");
            } catch (Exception e) {
                ClientUtility.sendIRCMessage(" Ошибка подключения: " + e.getMessage());
                Log.error("IRC handshake failed: " + e.getMessage());
            }
        }, "IRC-Handshake").start();
    }

    public void sendIRCMessage(String message) {
        if (message == null || message.isBlank()) return;

        if (LINK_PATTERN.matcher(message).find()) {
            ClientUtility.sendIRCMessage("Запрещено отправлять ссылки");
            return;
        }

        if (SPAM_PATTERN.matcher(message).find()) {
            ClientUtility.sendIRCMessage("Спам запрещён");
            return;
        }

        if (message.length() > 200) {
            ClientUtility.sendIRCMessage("Сообщение слишком длинное (макс. 200 символов)");
            return;
        }

        new Thread(() -> {
            try {
                JsonObject request = new JsonObject();
                request.addProperty("action", "irc:message");
                request.addProperty("apiKey", Constants.API_KEY);
                request.addProperty("username", Profile.getUsername());
                request.addProperty("message", message);

                connection.sendRequest(request);
                displayMessage(Profile.getUsername(), Profile.getRole(), message);
            } catch (Exception e) {
                ClientUtility.sendIRCMessage("Ошибка отправки: " + e.getMessage());
                Log.error("IRC send error: " + e.getMessage());
            }
        }, "IRC-Send").start();
    }

    private String formatSender(String username, String role) {
        String name = (username != null && !username.isBlank() ? username : "user");

        String prefix = switch (role != null ? role : "") {
            case "Разработчик", "Администратор" -> "§cАдмин";
            case "Ютубер" -> "§cЮтубер";
            case "Модератор" -> "§9Модератор";
            default -> "§7Пользователь";
        };

        return prefix + " §f" + name;
    }

    private void displayMessage(String username, String role, String message) {
        ClientUtility.sendIRCMessage(formatSender(username, role) + ": " + message);
    }

    public void shutdown() {
        try {
            connection.disconnect();
        } catch (Exception e) {
            Log.error("IRC shutdown error: " + e.getMessage());
        }
    }
}