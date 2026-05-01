package itz.silentcore.web.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import itz.silentcore.utils.client.Constants;
import itz.silentcore.utils.client.Log;

public class CloudConfigs {
    private static final ServerConnection connection = ServerConnection.getInstance();

    public static String uploadConfig(String owner, String data) {
        try {
            JsonObject request = new JsonObject();
            request.addProperty("action", "config:upload");
            request.addProperty("apiKey", Constants.API_KEY);
            request.addProperty("owner", owner);
            request.addProperty("data", data);

            String response = connection.sendRequest(request);
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

            String status = jsonResponse.get("status").getAsString();
            if ("success".equals(status)) {
                return jsonResponse.get("key").getAsString();
            } else if ("exists".equals(status)) {
                return "EXISTS:" + jsonResponse.get("key").getAsString();
            }
            return null;
        } catch (Exception e) {
            Log.error("Config upload error: " + e.getMessage());
            return null;
        }
    }

    public static String downloadConfig(String owner, String key) {
        try {
            JsonObject request = new JsonObject();
            request.addProperty("action", "config:download");
            request.addProperty("apiKey", Constants.API_KEY);
            request.addProperty("owner", owner);
            request.addProperty("key", key);

            String response = connection.sendRequest(request);
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

            if ("success".equals(jsonResponse.get("status").getAsString())) {
                return jsonResponse.get("data").getAsString();
            }
            return null;
        } catch (Exception e) {
            Log.error("Config download error: " + e.getMessage());
            return null;
        }
    }

    public static boolean deleteConfig(String owner, String key) {
        try {
            JsonObject request = new JsonObject();
            request.addProperty("action", "config:delete");
            request.addProperty("apiKey", Constants.API_KEY);
            request.addProperty("owner", owner);
            request.addProperty("key", key);

            String response = connection.sendRequest(request);
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

            return "success".equals(jsonResponse.get("status").getAsString());
        } catch (Exception e) {
            Log.error("Config delete error: " + e.getMessage());
            return false;
        }
    }
}
