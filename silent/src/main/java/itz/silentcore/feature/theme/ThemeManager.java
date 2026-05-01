package itz.silentcore.feature.theme;

import itz.silentcore.utils.render.ColorRGBA;
import lombok.Getter;
import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    private static ThemeManager instance;
    
    @Getter
    private final List<Theme> themes = new ArrayList<>();
    
    @Getter
    private Theme currentTheme;
    
    private ThemeManager() {
        // Добавляем все темы
        themes.add(Theme.purple());
        themes.add(Theme.blue());
        themes.add(Theme.red());
        themes.add(Theme.green());
        themes.add(Theme.orange());
        
        // По умолчанию фиолетовая тема
        currentTheme = themes.get(0);
    }
    
    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    public void setTheme(Theme theme) {
        this.currentTheme = theme;
    }
    
    public void setTheme(String name) {
        themes.stream()
            .filter(t -> t.getName().equalsIgnoreCase(name))
            .findFirst()
            .ifPresent(this::setTheme);
    }
    
    public int getPrimaryColor() {
        return currentTheme.getPrimaryColor();
    }
    
    public int getSecondaryColor() {
        return currentTheme.getSecondaryColor();
    }
    
    public int getAccentColor() {
        return currentTheme.getAccentColor();
    }
    
    public int getBackgroundColor() {
        return currentTheme.getBackgroundColor();
    }
    
    // Методы для получения ColorRGBA с альфа-каналом
    public ColorRGBA getPrimaryColorRGBA(int alpha) {
        int color = getPrimaryColor();
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return ColorRGBA.of(r, g, b, alpha);
    }
    
    public ColorRGBA getPrimaryColorRGBA() {
        return getPrimaryColorRGBA(255);
    }
}
