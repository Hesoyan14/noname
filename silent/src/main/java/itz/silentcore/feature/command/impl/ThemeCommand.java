package itz.silentcore.feature.command.impl;

import itz.silentcore.feature.command.Command;
import itz.silentcore.utils.other.DisplayName;
import itz.silentcore.utils.other.ThemeList;
import itz.silentcore.utils.client.ClientUtility;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ThemeCommand extends Command {
    private static final int THEMES_PER_PAGE = 8;

    public ThemeCommand() {
        super("themes", "Установка своей темы в клиенте", ".themes");
    }

    @Override
    public void execute(String[] args) {
        List<String> themeNames = getThemeNames();

        if (args.length == 0) {
            displayThemesPage(themeNames, 1);
            ClientUtility.sendMessage("§7Использование: .themes list [страница] | .themes <название>");
            return;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("list")) {
            int page = 1;
            if (args.length > 1) {
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    ClientUtility.sendMessage("§cНеверный номер страницы");
                    return;
                }
            }
            displayThemesPage(themeNames, page);
            return;
        }

        String[] colors = getThemeColors(subCommand.toUpperCase());
        if (colors == null) {
            ClientUtility.sendMessage("§cТема '" + subCommand + "' не найдена.");
            return;
        }

        ClientUtility.setCurrentTheme(subCommand.toUpperCase());
        String display = getDisplayName(subCommand.toUpperCase());
        ClientUtility.sendMessage("§aТема " + display + " успешно применена!");
    }

    private void displayThemesPage(List<String> themeNames, int page) {
        int totalPages = (themeNames.size() + THEMES_PER_PAGE - 1) / THEMES_PER_PAGE;

        if (page < 1 || page > totalPages) {
            ClientUtility.sendMessage("§cНеверный номер страницы. Доступно страниц: " + totalPages);
            return;
        }

        int startIdx = (page - 1) * THEMES_PER_PAGE;
        int endIdx = Math.min(startIdx + THEMES_PER_PAGE, themeNames.size());

        ClientUtility.sendMessage("§6Доступные темы (страница " + page + "/" + totalPages + "):");

        for (int i = startIdx; i < endIdx; i++) {
            String themeName = themeNames.get(i);
            String display = getDisplayName(themeName);
            String lower = themeName.toLowerCase();
            ClientUtility.sendMessage(" §f• " + display + " §7(.themes " + lower + ")");
        }

        if (totalPages > 1) {
            String pageInfo = "§7Страница " + page + "/" + totalPages;
            if (page > 1) pageInfo += " | Пред: .themes list " + (page - 1);
            if (page < totalPages) pageInfo += " | След: .themes list " + (page + 1);
            ClientUtility.sendMessage(pageInfo);
        }
    }

    @Override
    public List<String> getSuggestions(String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            suggestions.add("list");
            for (String themeName : getThemeNames()) {
                String lower = themeName.toLowerCase();
                if (lower.startsWith(partial)) suggestions.add(lower);
            }
        } else if (args.length == 2 && args[0].toLowerCase().equals("list")) {
            List<String> themeNames = getThemeNames();
            int totalPages = (themeNames.size() + THEMES_PER_PAGE - 1) / THEMES_PER_PAGE;
            for (int i = 1; i <= totalPages; i++) {
                suggestions.add(String.valueOf(i));
            }
        }
        return suggestions;
    }

    private List<String> getThemeNames() {
        List<String> names = new ArrayList<>();
        for (Field f : ThemeList.class.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers()) && f.getType() == String.class) {
                try {
                    String value = (String) f.get(null);
                    if (value.matches("(#?[0-9A-Fa-f]{6})( #?[0-9A-Fa-f]{6})?")) {
                        names.add(f.getName());
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

    public static String[] getThemeColors(String themeName) {
        try {
            Field f = ThemeList.class.getField(themeName);
            if (f.getType() == String.class && java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                String value = (String) f.get(null);
                return value.trim().split("\\s+");
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
        return null;
    }
}