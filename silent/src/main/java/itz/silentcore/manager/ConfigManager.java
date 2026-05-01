package itz.silentcore.manager;

import com.google.gson.*;
import itz.silentcore.SilentCore;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.utils.client.ClientUtility;
import lombok.Getter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    @Getter
    private final File configsDirectory;
    @Getter
    public final Gson gson;

    public ConfigManager() {
        this.configsDirectory = new File(getsilentcoreFolderPath(), "Configs");
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        if (!configsDirectory.exists()) {
            configsDirectory.mkdirs();
        }
    }

    private File getsilentcoreFolderPath() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return new File("C:\\Silent");
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            String userHome = System.getProperty("user.home");
            return new File(userHome + File.separator + "SilentCore");
        } else if (os.contains("mac")) {
            String userHome = System.getProperty("user.home");
            return new File(userHome + File.separator + "SilentCore");
        } else {
            String userHome = System.getProperty("user.home");
            return new File(userHome + File.separator + "SilentCore");
        }
    }

    public boolean saveConfig(String configName) {
        try {
            File configFile = new File(configsDirectory, configName + ".silent");
            JsonObject configData = new JsonObject();
            JsonObject modulesData = new JsonObject();

            for (Module module : SilentCore.getInstance().moduleManager.getModules()) {
                modulesData.add(module.getName(), module.save());
            }

            configData.add("modules", modulesData);
            configData.addProperty("configName", configName);
            configData.addProperty("savedAt", System.currentTimeMillis());

            try (FileWriter writer = new FileWriter(configFile)) {
                gson.toJson(configData, writer);
            }

            ClientUtility.sendMessage("§fКонфигурация " + configName + " успешно сохранена!");
            return true;
        } catch (Exception e) {
            ClientUtility.sendMessage("§fОшибка при сохранении конфигурации: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean loadConfig(String configName) {
        try {
            File configFile = new File(configsDirectory, configName + ".silent");

            if (!configFile.exists()) {
                ClientUtility.sendMessage("§fКонфигурация " + configName + " не найдена!");
                return false;
            }

            try (FileReader reader = new FileReader(configFile)) {
                JsonObject configData = gson.fromJson(reader, JsonObject.class);

                if (configData == null || !configData.has("modules")) {
                    ClientUtility.sendMessage("§fНеверный формат конфигурации!");
                    return false;
                }

                JsonObject modulesData = configData.getAsJsonObject("modules");

                for (Module module : SilentCore.getInstance().moduleManager.getModules()) {
                    if (modulesData.has(module.getName())) {
                        module.load(modulesData.getAsJsonObject(module.getName()));
                    }
                }

                ClientUtility.sendMessage("§fКонфигурация " + configName + " успешно загружена!");
                return true;
            }
        } catch (Exception e) {
            ClientUtility.sendMessage("§fОшибка при загрузке конфигурации: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteConfig(String configName) {
        try {
            File configFile = new File(configsDirectory, configName + ".silentcore");

            if (!configFile.exists()) {
                ClientUtility.sendMessage("§fКонфигурация " + configName + " не найдена!");
                return false;
            }

            if (configFile.delete()) {
                ClientUtility.sendMessage("§fКонфигурация " + configName + " успешно удалена!");
                return true;
            } else {
                ClientUtility.sendMessage("§fНе удалось удалить конфигурацию!");
                return false;
            }
        } catch (Exception e) {
            ClientUtility.sendMessage("§fОшибка при удалении конфигурации: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getConfigList() {
        List<String> configs = new ArrayList<>();
        File[] files = configsDirectory.listFiles((dir, name) -> name.endsWith(".silentcore"));

        if (files != null) {
            for (File file : files) {
                configs.add(file.getName().replace(".silentcore", ""));
            }
        }

        return configs;
    }

    public void listConfigs() {
        List<String> configs = getConfigList();

        if (configs.isEmpty()) {
            ClientUtility.sendMessage("§fНет сохранённых конфигураций");
            return;
        }

        ClientUtility.sendMessage("§fДоступные конфигурации:");
        for (String config : configs) {
            ClientUtility.sendMessage("§f- " + config);
        }
    }
}
