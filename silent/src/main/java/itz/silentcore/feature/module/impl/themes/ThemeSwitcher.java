package itz.silentcore.feature.module.impl.themes;

import itz.silentcore.feature.module.api.Category;
import itz.silentcore.feature.module.api.Module;
import itz.silentcore.feature.module.api.ModuleAnnotation;
import itz.silentcore.feature.module.api.setting.impl.ModeSetting;
import itz.silentcore.utils.client.ClientUtility;
import itz.silentcore.utils.other.DisplayName;
import itz.silentcore.utils.other.ThemeList;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@ModuleAnnotation(name = "Theme Switcher", category = Category.THEMES)
public class ThemeSwitcher extends Module {
    
    private final ModeSetting themeSetting;
    
    public ThemeSwitcher() {
        List<String> themeNames = getThemeNames();
        String[] themes = themeNames.toArray(new String[0]);
        
        themeSetting = new ModeSetting("Theme", themes);
    }
    
    @Override
    public void onEnable() {
        String selectedTheme = themeSetting.get();
        
        // Находим оригинальное имя поля по display name
        String fieldName = findFieldNameByDisplayName(selectedTheme);
        if (fieldName != null) {
            ClientUtility.setCurrentTheme(fieldName);
            ClientUtility.sendMessage("§aТема " + selectedTheme + " применена!");
        }
        
        toggle();
    }
    
    private List<String> getThemeNames() {
        List<String> names = new ArrayList<>();
        for (Field f : ThemeList.class.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers()) && f.getType() == String.class) {
                try {
                    String value = (String) f.get(null);
                    if (value.matches("(#?[0-9A-Fa-f]{6})( #?[0-9A-Fa-f]{6})?")) {
                        String displayName = getDisplayName(f.getName());
                        names.add(displayName);
                    }
                } catch (IllegalAccessException ignored) {
                }
            }
        }
        return names;
    }
    
    private String getDisplayName(String fieldName) {
        try {
            Field f = ThemeList.class.getField(fieldName);
            DisplayName ann = f.getAnnotation(DisplayName.class);
            return ann != null ? ann.value() : fieldName;
        } catch (NoSuchFieldException e) {
            return fieldName;
        }
    }
    
    private String findFieldNameByDisplayName(String displayName) {
        for (Field f : ThemeList.class.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers()) && f.getType() == String.class) {
                try {
                    String value = (String) f.get(null);
                    if (value.matches("(#?[0-9A-Fa-f]{6})( #?[0-9A-Fa-f]{6})?")) {
                        String fieldDisplayName = getDisplayName(f.getName());
                        if (fieldDisplayName.equals(displayName)) {
                            return f.getName();
                        }
                    }
                } catch (IllegalAccessException ignored) {
                }
            }
        }
        return null;
    }
}
