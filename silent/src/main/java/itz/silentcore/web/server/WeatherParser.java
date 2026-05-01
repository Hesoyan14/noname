package itz.silentcore.web.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import itz.silentcore.utils.client.Constants;
import itz.silentcore.utils.client.Log;

import java.util.concurrent.ConcurrentHashMap;

public final class WeatherParser {
    private static final ServerConnection connection = ServerConnection.getInstance();
    private static final String DEFAULT_WEATHER = "Н/Д";
    private static final long CACHE_DURATION_MS = 30 * 60 * 1000; // 30 minutes instead of 10
    private static final ConcurrentHashMap<String, CachedWeather> cache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> lastUpdateAttempt = new ConcurrentHashMap<>();
    private static final long UPDATE_COOLDOWN_MS = 60 * 1000; // Don't retry within 60 seconds
    private static String userCity = null;

    private WeatherParser() {}

    public static String weather() {
        String city = getUserCity();
        return getWeather(city);
    }

    private static String getUserCity() {
        if (userCity != null) {
            return userCity;
        }

        try {
            JsonObject request = new JsonObject();
            request.addProperty("action", "geo:get");
            request.addProperty("apiKey", Constants.API_KEY);

            String response = connection.sendRequest(request);
            if (response != null && !response.isEmpty() && !response.equals("null")) {
                try {
                    JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
                    if (jsonResponse.has("city") && jsonResponse.get("city") != null) {
                        String city = jsonResponse.get("city").getAsString();
                        if (city != null && !city.isEmpty()) {
                            userCity = city;
                            return userCity;
                        }
                    }
                } catch (Exception parseException) {
                    Log.print("Не удалось парсить ответ о городе: " + parseException.getMessage() + ", используется Москва");
                }
            } else {
                Log.print("Пустой ответ от сервера о городе, используется Москва");
            }
        } catch (Exception e) {
            Log.error("Не удалось определить город пользователя: " + e.getMessage());
        }

        userCity = "Москва";
        return userCity;
    }

    public static String getWeather(String city) {
        CachedWeather cached = cache.get(city);
        if (cached != null && !cached.isExpired()) {
            return cached.temperature;
        }

        // Check if we've already attempted an update recently
        Long lastAttempt = lastUpdateAttempt.get(city);
        long now = System.currentTimeMillis();
        if (lastAttempt != null && (now - lastAttempt) < UPDATE_COOLDOWN_MS) {
            // Still in cooldown period, don't try again
            return cached != null ? cached.temperature : DEFAULT_WEATHER;
        }

        if (connection.isConnected()) {
            lastUpdateAttempt.put(city, now);
            new Thread(() -> updateWeathersilentcore(city), "Weather-Update").start();
        }

        return cached != null ? cached.temperature : DEFAULT_WEATHER;
    }

    private static void updateWeathersilentcore(String city) {
        try {

            if (!connection.isConnected()) {
                Log.print("Пропуск обновления погоды: соединение не установлено");
                return;
            }

            JsonObject request = new JsonObject();
            request.addProperty("action", "weather:get");
            request.addProperty("apiKey", Constants.API_KEY);
            request.addProperty("city", city);

            String response = connection.sendRequest(request);
            if (response == null || response.isEmpty() || response.equals("null")) {
                Log.error("API погоды вернул пустой ответ");
                return;
            }

            try {
                JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

                if (jsonResponse.has("status") && jsonResponse.get("status") != null
                    && "success".equals(jsonResponse.get("status").getAsString())) {
                    if (jsonResponse.has("temperature") && jsonResponse.get("temperature") != null) {
                        String temperature = jsonResponse.get("temperature").getAsString();
                        cache.put(city, new CachedWeather(temperature));
                    } else {
                        Log.error("В ответе погоды отсутствует поле temperature");
                    }
                } else {
                    String message = (jsonResponse.has("message") && jsonResponse.get("message") != null)
                        ? jsonResponse.get("message").getAsString()
                        : "Неизвестная ошибка";
                    Log.error("Ошибка API погоды: " + message);
                }
            } catch (Exception parseException) {
                Log.error("Ошибка парсинга ответа погоды: " + parseException.getMessage());
            }
        } catch (Exception e) {
            Log.error("Ошибка получения погоды: " + e.getMessage());
        }
    }

    private static class CachedWeather {
        String temperature;
        long timestamp;

        CachedWeather(String temperature) {
            this.temperature = temperature;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS;
        }
    }
}