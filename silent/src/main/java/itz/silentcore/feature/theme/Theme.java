package itz.silentcore.feature.theme;

import lombok.Getter;

@Getter
public class Theme {
    private final String name;
    private final int primaryColor;
    private final int secondaryColor;
    private final int accentColor;
    private final int backgroundColor;
    
    public Theme(String name, int primaryColor, int secondaryColor, int accentColor, int backgroundColor) {
        this.name = name;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.accentColor = accentColor;
        this.backgroundColor = backgroundColor;
    }
    
    public static Theme purple() {
        return new Theme("Purple", 
            0xFFA581EE,  // Primary - фиолетовый
            0xFF8B6FD9,  // Secondary - темнее фиолетовый
            0xFFB99BFF,  // Accent - светлее фиолетовый
            0xFF1A1A2E); // Background - темный
    }
    
    public static Theme blue() {
        return new Theme("Blue",
            0xFF4A9EFF,  // Primary - синий
            0xFF3A7FCC,  // Secondary - темнее синий
            0xFF6BB6FF,  // Accent - светлее синий
            0xFF1A1E2E); // Background - темный
    }
    
    public static Theme red() {
        return new Theme("Red",
            0xFFFF4A6E,  // Primary - красный
            0xFFCC3A57,  // Secondary - темнее красный
            0xFFFF6B8A,  // Accent - светлее красный
            0xFF2E1A1A); // Background - темный
    }
    
    public static Theme green() {
        return new Theme("Green",
            0xFF4AFF7C,  // Primary - зеленый
            0xFF3ACC63,  // Secondary - темнее зеленый
            0xFF6BFF9B,  // Accent - светлее зеленый
            0xFF1A2E1E); // Background - темный
    }
    
    public static Theme orange() {
        return new Theme("Orange",
            0xFFFF8A4A,  // Primary - оранжевый
            0xFFCC6E3A,  // Secondary - темнее оранжевый
            0xFFFFA56B,  // Accent - светлее оранжевый
            0xFF2E221A); // Background - темный
    }
}
