package code.essence.utils.client.managers.file.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import code.essence.features.impl.misc.autobuy.parser.FunTimePriceParser;
import code.essence.features.impl.misc.autobuy.parser.HolyWorldPriceParser;
import code.essence.features.impl.misc.autobuy.parser.SpookyTimePriceParser;
import code.essence.display.screens.clickgui.components.implement.autobuy.items.AutoBuyableItem;
import code.essence.display.screens.clickgui.components.implement.autobuy.originalitems.ItemRegistry;
import code.essence.display.screens.autobuy.AutoBuyScreen;
import code.essence.features.impl.misc.autobuy.AutoBuyBanList;
import code.essence.utils.client.managers.file.ClientFile;
import code.essence.utils.client.managers.file.exception.FileLoadException;
import code.essence.utils.client.managers.file.exception.FileSaveException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AutoBuyConfigFile extends ClientFile {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String PARSER_SETTINGS_KEY = "parserSettings";

    public AutoBuyConfigFile() {
        super("autobuy");
    }

    @Override
    public void loadFromFile(File path) throws FileLoadException {
        File file = new File(path, getName() + ".json");
        if (!file.exists()) {
            
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            if (json != null) {
                
                if (json.has("abBanList") && json.get("abBanList").isJsonArray()) {
                    JsonArray arr = json.getAsJsonArray("abBanList");
                    List<String> banList = new ArrayList<>();
                    for (JsonElement el : arr) {
                        if (el != null && el.isJsonPrimitive()) {
                            try {
                                String s = el.getAsString();
                                if (s != null && !s.trim().isEmpty()) {
                                    banList.add(s.trim());
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    AutoBuyBanList.loadFromList(banList);
                }

                
                boolean hasNewServerFormat = json.has("FUNTIME") || json.has("HOLYWORLD") || json.has("SPOOKYTIME");

                if (hasNewServerFormat) {
                    
                    loadServerItemsFromJson(json, "FUNTIME", ItemRegistry.getFunTimeItems());
                    loadServerItemsFromJson(json, "HOLYWORLD", ItemRegistry.getHolyWorld());
                    loadServerItemsFromJson(json, "SPOOKYTIME", ItemRegistry.getSpookyTime());
                } else {
                    
                    boolean hasNewFormat = json.has("funTimeItems") || json.has("holyWorldItems") || json.has("spookyTimeItems");
                    if (hasNewFormat) {
                        
                        if (json.has("funTimeItems")) {
                            loadItemsFromJson(json.getAsJsonObject("funTimeItems"), "FUNTIME");
                        }
                        if (json.has("holyWorldItems")) {
                            loadItemsFromJson(json.getAsJsonObject("holyWorldItems"), "HOLYWORLD");
                        }
                        if (json.has("spookyTimeItems")) {
                            loadItemsFromJson(json.getAsJsonObject("spookyTimeItems"), "SPOOKYTIME");
                        }
                    } else {
                        
                        for (AutoBuyableItem item : ItemRegistry.getAllItems()) {
                            String itemName = item.getDisplayName();
                            if (json.has(itemName)) {
                                var itemData = json.get(itemName);
                                if (itemData.isJsonObject()) {
                                    JsonObject itemObj = itemData.getAsJsonObject();
                                    item.setEnabled(itemObj.has("enabled") && itemObj.get("enabled").getAsBoolean());
                                    if (itemObj.has("price")) {
                                        int price = itemObj.get("price").getAsInt();
                                        
                                        if (itemName != null) {
                                            AutoBuyScreen.INSTANCE.getCustomPrices().entrySet().removeIf(entry -> {
                                                AutoBuyableItem key = entry.getKey();
                                                return key != null && key.getDisplayName() != null && key.getDisplayName().equals(itemName);
                                            });
                                        }
                                        AutoBuyScreen.INSTANCE.getCustomPrices().put(item, price);
                                        
                                        if (price > 0) {
                                            item.getSettings().setBuyBelow(price);
                                        }
                                    }
                                } else {
                                    
                                    item.setEnabled(itemData.getAsBoolean());
                                }
                            }
                        }
                    }

                    
                    if (json.has(PARSER_SETTINGS_KEY) && json.get(PARSER_SETTINGS_KEY).isJsonObject()) {
                        JsonObject parserObj = json.getAsJsonObject(PARSER_SETTINGS_KEY);

                        
                        if (parserObj.has("discountPercent")) {
                            try {
                                FunTimePriceParser.setDiscountPercent(parserObj.get("discountPercent").getAsInt());
                            } catch (Exception ignored) {
                            }
                        }
                        
                        if (parserObj.has("funTimeDiscountPercent")) {
                            try {
                                FunTimePriceParser.setDiscountPercent(parserObj.get("funTimeDiscountPercent").getAsInt());
                            } catch (Exception ignored) {
                            }
                        }
                        
                        if (parserObj.has("holyWorldDiscountPercent")) {
                            try {
                                HolyWorldPriceParser.setDiscountPercent(parserObj.get("holyWorldDiscountPercent").getAsInt());
                            } catch (Exception ignored) {
                            }
                        }
                        
                        if (parserObj.has("spookyTimeDiscountPercent")) {
                            try {
                                SpookyTimePriceParser.setDiscountPercent(parserObj.get("spookyTimeDiscountPercent").getAsInt());
                            } catch (Exception ignored) {
                            }
                        }

                        
                        java.util.Map<String, java.util.Set<String>> parserItemsByServer = new java.util.HashMap<>();

                        
                        boolean hasNewParserFormat = parserObj.has("funTimeParserItems") ||
                                parserObj.has("holyWorldParserItems") ||
                                parserObj.has("spookyTimeParserItems");
                        if (hasNewParserFormat) {
                            
                            if (parserObj.has("funTimeParserItems") && parserObj.get("funTimeParserItems").isJsonArray()) {
                                java.util.Set<String> items = new java.util.HashSet<>();
                                JsonArray arr = parserObj.getAsJsonArray("funTimeParserItems");
                                for (JsonElement el : arr) {
                                    if (el != null && el.isJsonPrimitive()) {
                                        try {
                                            String s = el.getAsString();
                                            if (s != null && !s.trim().isEmpty()) {
                                                items.add(s.trim().toLowerCase());
                                            }
                                        } catch (Exception ignored) {
                                        }
                                    }
                                }
                                parserItemsByServer.put("FUNTIME", items);
                            }
                            if (parserObj.has("holyWorldParserItems") && parserObj.get("holyWorldParserItems").isJsonArray()) {
                                java.util.Set<String> items = new java.util.HashSet<>();
                                JsonArray arr = parserObj.getAsJsonArray("holyWorldParserItems");
                                for (JsonElement el : arr) {
                                    if (el != null && el.isJsonPrimitive()) {
                                        try {
                                            String s = el.getAsString();
                                            if (s != null && !s.trim().isEmpty()) {
                                                items.add(s.trim().toLowerCase());
                                            }
                                        } catch (Exception ignored) {
                                        }
                                    }
                                }
                                parserItemsByServer.put("HOLYWORLD", items);
                            }
                            if (parserObj.has("spookyTimeParserItems") && parserObj.get("spookyTimeParserItems").isJsonArray()) {
                                java.util.Set<String> items = new java.util.HashSet<>();
                                JsonArray arr = parserObj.getAsJsonArray("spookyTimeParserItems");
                                for (JsonElement el : arr) {
                                    if (el != null && el.isJsonPrimitive()) {
                                        try {
                                            String s = el.getAsString();
                                            if (s != null && !s.trim().isEmpty()) {
                                                items.add(s.trim().toLowerCase());
                                            }
                                        } catch (Exception ignored) {
                                        }
                                    }
                                }
                                parserItemsByServer.put("SPOOKYTIME", items);
                            }
                        } else if (parserObj.has("items") && parserObj.get("items").isJsonArray()) {
                            
                            java.util.Set<String> funTimeItems = new java.util.HashSet<>();
                            JsonArray arr = parserObj.getAsJsonArray("items");
                            for (JsonElement el : arr) {
                                if (el != null && el.isJsonPrimitive()) {
                                    try {
                                        String s = el.getAsString();
                                        if (s != null && !s.trim().isEmpty()) {
                                            funTimeItems.add(s.trim().toLowerCase());
                                        }
                                    } catch (Exception ignored) {
                                    }
                                }
                            }
                            parserItemsByServer.put("FUNTIME", funTimeItems);
                        }

                        
                        if (!parserItemsByServer.isEmpty()) {
                            AutoBuyScreen.INSTANCE.setParserEnabledItemsByServer(parserItemsByServer);
                        }

                        
                        AutoBuyScreen.INSTANCE.syncParserSettingsWithServerData();
                    }

                    
                    if (json.has("durabilitySettings")) {
                        JsonObject durabilityObj = json.getAsJsonObject("durabilitySettings");
                        for (String serverKey : durabilityObj.keySet()) {
                            JsonObject serverJson = durabilityObj.getAsJsonObject(serverKey);
                            if (serverJson != null) {
                                for (String itemName : serverJson.keySet()) {
                                    float durabilityValue = serverJson.get(itemName).getAsFloat();
                                    AutoBuyScreen.INSTANCE.setDurabilitySetting(serverKey, itemName, durabilityValue);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new FileLoadException("Failed to load " + getName() + " from file", e);
        }
    }

    @Override
    public void saveToFile(File path) throws FileSaveException {
        JsonObject json = new JsonObject();

        
        java.util.Map<String, java.util.Map<String, Float>> durabilitySettings = AutoBuyScreen.INSTANCE.getDurabilitySettingsByServer();
        java.util.Map<String, java.util.Set<String>> parserItemsByServer = AutoBuyScreen.INSTANCE.getParserEnabledItemsByServer();

        
        saveServerItems(json, "FUNTIME", ItemRegistry.getFunTimeItems(), durabilitySettings, parserItemsByServer);
        saveServerItems(json, "HOLYWORLD", ItemRegistry.getHolyWorld(), durabilitySettings, parserItemsByServer);
        saveServerItems(json, "SPOOKYTIME", ItemRegistry.getSpookyTime(), durabilitySettings, parserItemsByServer);

        
        JsonObject parserObj = new JsonObject();
        parserObj.addProperty("funTimeDiscountPercent", FunTimePriceParser.getDiscountPercent());
        parserObj.addProperty("holyWorldDiscountPercent", HolyWorldPriceParser.getDiscountPercent());
        parserObj.addProperty("spookyTimeDiscountPercent", SpookyTimePriceParser.getDiscountPercent());
        json.add(PARSER_SETTINGS_KEY, parserObj);

        
        JsonArray abBanList = new JsonArray();
        for (String name : AutoBuyBanList.getBannedPlayers()) {
            abBanList.add(name);
        }
        json.add("abBanList", abBanList);

        File file = new File(path, getName() + ".json");
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(json, writer);
        } catch (IOException e) {
            throw new FileSaveException("Failed to save " + getName() + " to file", e);
        }
    }

    
    private void saveServerItems(JsonObject json, String serverKey, List<AutoBuyableItem> items,
                                 java.util.Map<String, java.util.Map<String, Float>> durabilitySettings,
                                 java.util.Map<String, java.util.Set<String>> parserItemsByServer) {
        JsonObject serverObj = new JsonObject();

        java.util.Set<String> parserItems = parserItemsByServer.getOrDefault(serverKey, new java.util.HashSet<>());

        for (AutoBuyableItem item : items) {
            if (item == null || item.getDisplayName() == null) continue;

            JsonObject itemObj = new JsonObject();

            
            itemObj.addProperty("enabled", item.isEnabled());

            
            
            int price = AutoBuyScreen.INSTANCE.getCustomPrices().containsKey(item)
                    ? AutoBuyScreen.INSTANCE.getCustomPrices().get(item)
                    : item.getPrice();
            itemObj.addProperty("цена", price);

            
            String itemNameLower = item.getDisplayName().toLowerCase();
            boolean parserEnabled = parserItems.contains(itemNameLower);
            itemObj.addProperty("парсер", parserEnabled);

            
            float durability = 0f;
            java.util.Map<String, Float> serverDurability = durabilitySettings.get(serverKey);
            if (serverDurability != null) {
                Float savedDurability = serverDurability.get(item.getDisplayName().toLowerCase());
                if (savedDurability != null) {
                    durability = savedDurability;
                }
            }
            itemObj.addProperty("прочность", durability);

            serverObj.add(item.getDisplayName(), itemObj);
        }

        json.add(serverKey, serverObj);
    }

    
    private void loadServerItemsFromJson(JsonObject json, String serverKey, List<AutoBuyableItem> items) {
        if (!json.has(serverKey)) return;

        JsonObject serverObj = json.getAsJsonObject(serverKey);
        if (serverObj == null) return;

        
        java.util.Set<String> parserItems = new java.util.HashSet<>();
        java.util.Map<String, Float> durabilityItems = new java.util.HashMap<>();

        for (AutoBuyableItem item : items) {
            if (item == null || item.getDisplayName() == null) continue;

            String itemName = item.getDisplayName();
            if (serverObj.has(itemName)) {
                JsonObject itemObj = serverObj.getAsJsonObject(itemName);
                if (itemObj != null) {
                    
                    if (itemObj.has("enabled")) {
                        item.setEnabled(itemObj.get("enabled").getAsBoolean());
                    }

                    
                    if (itemObj.has("цена")) {
                        int price = itemObj.get("цена").getAsInt();
                        
                        AutoBuyScreen.INSTANCE.getCustomPrices().entrySet().removeIf(entry -> {
                            AutoBuyableItem key = entry.getKey();
                            return key != null && key.getDisplayName() != null && key.getDisplayName().equals(itemName);
                        });
                        
                        
                        AutoBuyScreen.INSTANCE.getCustomPrices().put(item, price);
                        
                        
                        item.getSettings().setBuyBelow(price > 0 ? price : item.getPrice());
                    }

                    
                    if (itemObj.has("парсер")) {
                        boolean parserEnabled = itemObj.get("парсер").getAsBoolean();
                        if (parserEnabled) {
                            parserItems.add(itemName.toLowerCase());
                        }
                        
                        
                    }

                    
                    if (itemObj.has("прочность")) {
                        float durability = itemObj.get("прочность").getAsFloat();
                        if (durability > 0) {
                            durabilityItems.put(itemName.toLowerCase(), durability);
                        }
                    }
                }
            }
        }

        
        
        java.util.Map<String, java.util.Set<String>> parserItemsByServer = AutoBuyScreen.INSTANCE.getParserEnabledItemsByServer();
        parserItemsByServer.put(serverKey, parserItems);
        AutoBuyScreen.INSTANCE.setParserEnabledItemsByServer(parserItemsByServer);

        
        AutoBuyScreen.INSTANCE.syncParserSettingsWithServerData();

        
        if (!durabilityItems.isEmpty()) {
            for (java.util.Map.Entry<String, Float> entry : durabilityItems.entrySet()) {
                AutoBuyScreen.INSTANCE.setDurabilitySetting(serverKey, entry.getKey(), entry.getValue());
            }
        }
    }

    
    private void loadItemsFromJson(JsonObject itemsJson, String serverKey) {
        if (itemsJson == null) return;

        java.util.Set<String> parserItems = new java.util.HashSet<>();

        for (AutoBuyableItem item : ItemRegistry.getAllItems()) {
            if (item == null || item.getDisplayName() == null) continue;

            String itemName = item.getDisplayName();
            if (itemsJson.has(itemName)) {
                JsonElement itemData = itemsJson.get(itemName);
                if (itemData.isJsonObject()) {
                    JsonObject itemObj = itemData.getAsJsonObject();
                    item.setEnabled(itemObj.has("enabled") && itemObj.get("enabled").getAsBoolean());
                    if (itemObj.has("price")) {
                        int price = itemObj.get("price").getAsInt();
                        AutoBuyScreen.INSTANCE.getCustomPrices().entrySet().removeIf(entry -> {
                            AutoBuyableItem key = entry.getKey();
                            return key != null && key.getDisplayName() != null && key.getDisplayName().equals(itemName);
                        });
                        AutoBuyScreen.INSTANCE.getCustomPrices().put(item, price);
                        if (price > 0) {
                            item.getSettings().setBuyBelow(price);
                        }
                    }
                } else {
                    item.setEnabled(itemData.getAsBoolean());
                }
            }
        }
    }
}