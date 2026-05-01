package com.example.client.feature.module.api;

public enum Category {
    COMBAT("Combat", "h"),
    MOVEMENT("Movement", "g"),
    PLAYER("Player", "f"),
    RENDER("Render", "e"),
    MISC("Misc", "P");

    private final String name;
    private final String icon;

    Category(String name, String icon) {
        this.name = name;
        this.icon = icon;
    }

    public String getName() { return name; }
    public String getIcon() { return icon; }
}
