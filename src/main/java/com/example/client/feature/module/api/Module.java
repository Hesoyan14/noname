package com.example.client.feature.module.api;

import com.example.client.NoNameClient;
import com.example.client.utils.client.IMinecraft;

public class Module implements IMinecraft {
    protected final ModuleAnnotation info = this.getClass().getAnnotation(ModuleAnnotation.class);
    private String name;
    private final Category category;
    private boolean enabled;
    private int key = -1;

    protected Module() {
        this.name = info.name();
        this.category = info.category();
    }

    public String getName() { return name; }
    public Category getCategory() { return category; }
    public boolean isEnabled() { return enabled; }
    public int getKey() { return key; }
    public void setKey(int key) { this.key = key; }
    public ModuleAnnotation getInfo() { return info; }

    public void cToggle() {
        enabled = !enabled;
        if (enabled) {
            NoNameClient.getInstance().getEventBus().register(this);
            onEnable();
        } else {
            NoNameClient.getInstance().getEventBus().unregister(this);
            onDisable();
        }
    }

    public void setState(boolean state) {
        if (state == enabled) return;
        enabled = state;
        if (enabled) {
            NoNameClient.getInstance().getEventBus().register(this);
            onEnable();
        } else {
            NoNameClient.getInstance().getEventBus().unregister(this);
            onDisable();
        }
    }

    public void onEnable() {}
    public void onDisable() {}
}
