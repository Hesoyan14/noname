package itz.silentcore.feature.command.impl;

import com.google.gson.JsonObject;
import itz.silentcore.SilentCore;
import itz.silentcore.feature.command.Command;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.web.server.CloudConfigs;
import itz.silentcore.utils.client.ClientUtility;
import ru.kotopushka.compiler.sdk.classes.Profile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigCommand extends Command {

    private long lastUploadTime = 0;
    private static final long UPLOAD_COOLDOWN = 40000;

    public ConfigCommand() {
        super("config", "Управление конфигурациями", "cfg", "c");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            sendHelp();
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "save":
            case "s":
                if (args.length < 2) {
                    ClientUtility.sendMessage("§cИспользование: .config save <название>");
                    return;
                }
                saveAndUploadConfig(args[1]);
                break;

            case "load":
            case "l":
                if (args.length == 2) {
                    String arg = args[1];
                    if (isCloudKey(arg)) {
                        loadCloudConfig(Profile.getUsername(), arg);
                    } else {
                        SilentCore.getInstance().configManager.loadConfig(arg);
                    }
                } else if (args.length == 3) {
                    loadCloudConfig(args[1], args[2]);
                } else {
                    ClientUtility.sendMessage("§cИспользование: .config load <название> или .config load <ключ>");
                }
                break;

            case "delete":
            case "del":
            case "remove":
            case "rm":
            case "d":
                if (args.length < 2) {
                    ClientUtility.sendMessage("§cИспользование: .config delete <название> или .config delete <ключ>");
                    return;
                }
                String deleteArg = args[1];
                if (isCloudKey(deleteArg)) {
                    deleteCloudConfig(Profile.getUsername(), deleteArg);
                } else {
                    SilentCore.getInstance().configManager.deleteConfig(deleteArg);
                }
                break;

            case "list":
            case "ls":
                SilentCore.getInstance().configManager.listConfigs();
                break;

            case "dir":
            case "folder":
                openConfigDirectory();
                break;

            case "help":
            case "h":
                sendHelp();
                break;

            default:
                ClientUtility.sendMessage("§fНеизвестное действие: " + action);
                sendHelp();
                break;
        }
    }

    private boolean isCloudKey(String input) {
        return input.matches("^silentcore_[a-zA-Z0-9]{8}$");
    }

    private void openConfigDirectory() {
        new Thread(() -> {
            try {
                File configDir = SilentCore.getInstance().configManager.getConfigsDirectory();
                String os = System.getProperty("os.name").toLowerCase();

                if (os.contains("win")) {
                    Runtime.getRuntime().exec("explorer " + configDir.getAbsolutePath());
                } else if (os.contains("mac")) {
                    Runtime.getRuntime().exec("open " + configDir.getAbsolutePath());
                } else if (os.contains("nix") || os.contains("nux")) {
                    Runtime.getRuntime().exec("xdg-open " + configDir.getAbsolutePath());
                }
                ClientUtility.sendMessage("§fОткрыта папка с конфигами");
            } catch (Exception e) {
                ClientUtility.sendMessage("§fОшибка при открытии папки: " + e.getMessage());
            }
        }).start();
    }

    private void saveAndUploadConfig(String configName) {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastUpload = currentTime - lastUploadTime;

        if (timeSinceLastUpload < UPLOAD_COOLDOWN) {
            long remainingTime = (UPLOAD_COOLDOWN - timeSinceLastUpload) / 1000;
            ClientUtility.sendMessage("§fПодождите " + remainingTime + " секунд перед следующей загрузкой");
            return;
        }

        SilentCore.getInstance().configManager.saveConfig(configName);
        new Thread(() -> {
            try {
                Thread.sleep(100);
                uploadConfig(configName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void uploadConfig(String configName) {
        new Thread(() -> {
            try {
                JsonObject configData = new JsonObject();
                JsonObject modulesData = new JsonObject();

                for (Module module : SilentCore.getInstance().moduleManager.getModules()) {
                    modulesData.add(module.getName(), module.save());
                }

                configData.add("modules", modulesData);
                configData.addProperty("configName", configName);
                configData.addProperty("savedAt", System.currentTimeMillis());

                String jsonData = SilentCore.getInstance().configManager.getGson().toJson(configData);
                String owner = Profile.getUsername();
                String result = CloudConfigs.uploadConfig(owner, jsonData);

                if (result != null) {
                    if (result.startsWith("EXISTS:")) {
                        String existingKey = result.substring(7);
                        copyToClipboard(existingKey);
                        ClientUtility.sendMessage("§fТакой конфиг уже существует!");
                        ClientUtility.sendMessage("§fВаш ключ: §e" + existingKey + " §f(скопирован)");
                        ClientUtility.sendMessage("§fДля загрузки используйте: .config load " + existingKey);
                    } else {
                        lastUploadTime = System.currentTimeMillis();
                        copyToClipboard(result);
                        ClientUtility.sendMessage("§fКонфигурация загружена в облако!");
                        ClientUtility.sendMessage("§fВаш ключ: §e" + result + " §f(скопирован)");
                        ClientUtility.sendMessage("§fДля загрузки используйте: .config load " + result);
                    }
                } else {
                    ClientUtility.sendMessage("§fОшибка при загрузке конфигурации в облако");
                }
            } catch (Exception e) {
                ClientUtility.sendMessage("§fОшибка: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void copyToClipboard(String text) {
        try {
            net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
            mc.keyboard.setClipboard(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCloudConfig(String owner, String key) {
        new Thread(() -> {
            try {
                String jsonData = CloudConfigs.downloadConfig(owner, key);

                if (jsonData != null) {
                    JsonObject configData = SilentCore.getInstance().configManager.getGson().fromJson(jsonData, JsonObject.class);

                    if (configData != null && configData.has("modules")) {
                        JsonObject modulesData = configData.getAsJsonObject("modules");

                        for (Module module : SilentCore.getInstance().moduleManager.getModules()) {
                            if (modulesData.has(module.getName())) {
                                module.load(modulesData.getAsJsonObject(module.getName()));
                            }
                        }

                        ClientUtility.sendMessage("§fОблачная конфигурация загружена!");
                    } else {
                        ClientUtility.sendMessage("§fНеверный формат конфигурации!");
                    }
                } else {
                    ClientUtility.sendMessage("§fКонфигурация не найдена в облаке");
                }
            } catch (Exception e) {
                ClientUtility.sendMessage("§fОшибка: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void deleteCloudConfig(String owner, String key) {
        new Thread(() -> {
            try {
                boolean success = CloudConfigs.deleteConfig(owner, key);

                if (success) {
                    ClientUtility.sendMessage("§fОблачная конфигурация удалена!");
                } else {
                    ClientUtility.sendMessage("§fОшибка при удалении конфигурации из облака");
                }
            } catch (Exception e) {
                ClientUtility.sendMessage("§fОшибка: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void sendHelp() {
        ClientUtility.sendMessage("§f--- Команды конфигураций ---");
        ClientUtility.sendMessage("§f.config save <название> - Сохранить и загрузить в облако");
        ClientUtility.sendMessage("§f.config load <название> - Загрузить локально");
        ClientUtility.sendMessage("§f.config load <ключ> - Загрузить из облака");
        ClientUtility.sendMessage("§f.config delete/remove <название> - Удалить локальную конфигурацию");
        ClientUtility.sendMessage("§f.config delete/remove <ключ> - Удалить облачную конфигурацию");
        ClientUtility.sendMessage("§f.config list - Показать список конфигураций");
        ClientUtility.sendMessage("§f.config dir - Открыть папку с конфигами");
        ClientUtility.sendMessage("§fАлиасы: cfg, c | Удаление: del, d, remove, rm");
    }

    @Override
    public List<String> getSuggestions(String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 0 || (args.length == 1 && args[0].isEmpty())) {
            suggestions.add("save");
            suggestions.add("load");
            suggestions.add("delete");
            suggestions.add("list");
            suggestions.add("dir");
            suggestions.add("help");
            return suggestions;
        }

        String action = args[0].toLowerCase();

        if (args.length == 1) {
            if ("save".startsWith(action)) suggestions.add("save");
            if ("load".startsWith(action)) suggestions.add("load");
            if ("delete".startsWith(action)) suggestions.add("delete");
            if ("remove".startsWith(action)) suggestions.add("remove");
            if ("list".startsWith(action)) suggestions.add("list");
            if ("dir".startsWith(action)) suggestions.add("dir");
            if ("help".startsWith(action)) suggestions.add("help");
            return suggestions;
        }

        if (args.length == 2 && (action.equals("load") || action.equals("l") ||
                                  action.equals("delete") || action.equals("del") || action.equals("d") ||
                                  action.equals("remove") || action.equals("rm"))) {
            List<String> configs = SilentCore.getInstance().configManager.getConfigList();
            String partial = args[1].toLowerCase();

            for (String config : configs) {
                if (config.toLowerCase().startsWith(partial)) {
                    suggestions.add(action + " " + config);
                }
            }

            if (partial.isEmpty()) {
                for (String config : configs) {
                    suggestions.add(action + " " + config);
                }
            }
        }

        return suggestions;
    }
}