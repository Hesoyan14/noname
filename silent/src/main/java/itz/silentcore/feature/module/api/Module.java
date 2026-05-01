package itz.silentcore.feature.module.api;

import itz.silentcore.SilentCore;
import com.google.gson.*;
import itz.silentcore.feature.module.api.setting.Setting;
import itz.silentcore.utils.client.IMinecraft;
import itz.silentcore.feature.ui.hud.NotificationRenderer;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class Module implements IMinecraft, Comparable<Module> {
    protected ModuleAnnotation info = this.getClass().getAnnotation(ModuleAnnotation.class);
    private String name;
    private final Category category;
    private boolean enabled;
    private int key;

    protected Module() {
        name = info.name();
        category = info.category();
        enabled = false;
        key = -1;
    }

    public void setState(boolean state) {
        if (state) {
            enable();
        } else {
            disable();
        }

        enabled = state;
    }

    public void cToggle() {
        enabled = !enabled;
        toggle();

        if (enabled) {
            enable();
        } else {
            disable();
        }
    }

    public void toggle() {}
    public void onEnable() {}
    public void onDisable() {}

    public void enable() {
        SilentCore.getInstance().eventBus.register(this);
        onEnable();
        try { NotificationRenderer.pushModuleEnabled(this); } catch (Throwable ignored) {}
    }

    public void disable() {
        SilentCore.getInstance().eventBus.unregister(this);
        onDisable();
        try { NotificationRenderer.pushModuleDisabled(this); } catch (Throwable ignored) {}
    }

    public List<Setting> getSettings() {
        return Arrays.stream(getClass().getDeclaredFields()).map(field -> {
            try {
                field.setAccessible(true);
                return field.get(this);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }).filter(field -> field instanceof Setting).map(field -> (Setting) field).collect(Collectors.toList());
    }

    public JsonObject save() {
        JsonObject object = new JsonObject();
        object.addProperty("enabled", enabled);
        object.addProperty("key", key);
        JsonObject propertiesObject = new JsonObject();
        for (Setting setting : getSettings()) {
           setting.safe(propertiesObject);
        }
        object.add("settings", propertiesObject);
        return object;
    }

    public void load(JsonObject object) {
        try {
            if (object != null) {
                if (object.has("enabled")) {
                    boolean enable = object.get("enabled").getAsBoolean();
                    setState(enable);
                }
                if (object.has("key")) {
                    key = (object.get("key").getAsInt());
                }
                for (Setting setting : getSettings()) {
                    String valueOf = setting.getName();
                    JsonObject propertiesObject = object.getAsJsonObject("settings");
                    if (propertiesObject == null)
                        continue;
                    if (!propertiesObject.has(valueOf))
                        continue;
                    setting.load(propertiesObject);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int compareTo(@NotNull Module o) {
        return o.getName().compareTo(this.name);
    }
}

