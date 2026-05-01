package itz.silentcore.manager;

import com.google.common.eventbus.Subscribe;
import itz.silentcore.utils.client.Constants;
import itz.silentcore.feature.event.impl.WorldJoinEvent;
import itz.silentcore.utils.client.ClientUtility;
import lombok.Getter;
import lombok.Setter;
import ru.kotopushka.compiler.sdk.classes.Profile;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;

public class FirstLaunchManager {

    private static final String PAPKA = "SilentCore";
    @Setter
    @Getter
    private boolean isFirstLaunch;
    private boolean messageShown = false;

    public FirstLaunchManager() {
        checkFirstLaunch();
    }

    private void checkFirstLaunch() {
        File silentcoreFolder = new File(getsilentcoreFolderPath());
        isFirstLaunch = !silentcoreFolder.exists();
        if (isFirstLaunch) {
            silentcoreFolder.mkdirs();
        }
    }

    private String getsilentcoreFolderPath() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            return "C:\\" + PAPKA;
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            String userHome = System.getProperty("user.home");
            return userHome + File.separator + PAPKA;
        } else if (os.contains("mac")) {
            String userHome = System.getProperty("user.home");
            return userHome + File.separator + PAPKA;
        } else {
            String userHome = System.getProperty("user.home");
            return userHome + File.separator + PAPKA;
        }
    }

    @Subscribe
    public void onWorldJoin(WorldJoinEvent event) {
        if (isFirstLaunch && !messageShown) {
            messageShown = true;
            sendWelcomeMessage();
        }
    }

    private void sendWelcomeMessage() {
        String welcomeMessage = "Добро пожаловать в " + Constants.CLIENT_NAME + ", " + Profile.getUsername() +
                "\nДля использования команд используйте префикс ." +
                "\nДля открытия меню нажмите на правый Shift." +
                "\n\nТехническая поддержка - " + Constants.SUPPORT;

        ClientUtility.sendGradientMessage(welcomeMessage);
    }

    private void openTelegramLink() {
        new Thread(() -> {
            try {
                String os = System.getProperty("os.name").toLowerCase();
                String url = "https://" + Constants.TELEGRAM;

                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();

                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        desktop.browse(new URI(url));
                        return;
                    }
                }

                if (os.contains("win")) {
                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
                    return;
                }

                if (os.contains("nix") || os.contains("nux")) {
                    Runtime runtime = Runtime.getRuntime();
                    try {
                        runtime.exec(new String[]{"xdg-open", url});
                        return;
                    } catch (Exception e1) {
                        try {
                            runtime.exec(new String[]{"gnome-open", url});
                            return;
                        } catch (Exception e2) {
                            try {
                                runtime.exec(new String[]{"kde-open", url});
                            } catch (Exception e3) {
                            }
                        }
                    }
                }

                if (os.contains("mac")) {
                    Runtime.getRuntime().exec("open " + url);
                }

            } catch (Exception e) {
            }
        }, "BabkaPrankPriv").start();
    }

    public void resetFirstLaunch() {
        this.isFirstLaunch = true;
        this.messageShown = false;
    }
}