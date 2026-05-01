package itz.silentcore.feature.module.api;
import lombok.Getter;
@Getter
public enum Category {
    COMBAT("Combat","h"),
    MOVEMENT("Movement", "g"),
    PLAYER("Player", "f"),
    RENDER("Render", "e"),
    MISC("Misc", "P"),
    AUTOBUY("AutoBuy", "A"),
    THEMES("Themes", "T");

    @Getter
    private final String name;
    private final String icon;
    Category(String name, String icon) {
        this.name = name;
        this.icon = icon;
    }
}