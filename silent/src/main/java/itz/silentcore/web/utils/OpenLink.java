package itz.silentcore.web.utils;

import java.awt.*;
import java.net.URI;

public class OpenLink {

    public static void openLink(String domain) {
        if (domain == null || domain.trim().isEmpty()) {
            return;
        }

        final String url = normalizeUrl(domain.trim());

        new Thread(() -> {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        desktop.browse(new URI(url));
                        return;
                    }
                }

                String os = System.getProperty("os.name").toLowerCase();

                if (os.contains("win")) {
                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                    return;
                }

                if (os.contains("nix") || os.contains("nux") || os.contains("bsd")) {
                    Runtime runtime = Runtime.getRuntime();
                    String[] commands = {"xdg-open", "gnome-open", "kde-open", "gio open"};
                    for (String cmd : commands) {
                        try {
                            runtime.exec(new String[]{cmd, url});
                            return;
                        } catch (Exception ignored) {}
                    }
                }

                if (os.contains("mac")) {
                    Runtime.getRuntime().exec(new String[]{"open", url});
                }

            } catch (Exception ignored) {}
        }, "WebUtils").start();
    }

    private static String normalizeUrl(String input) {
        if (!input.matches("^https?://.*")) {
            return "https://" + input.replaceFirst("^https?://", "");
        }
        return input;
    }
}