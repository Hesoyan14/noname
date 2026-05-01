package itz.silentcore;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import itz.silentcore.discord.DiscordIntegration;
import itz.silentcore.feature.event.impl.TickEvent;
import itz.silentcore.feature.ui.hud.MediaPlayerRenderer;
import itz.silentcore.manager.AccountManager;
import itz.silentcore.manager.CommandManager;
import itz.silentcore.manager.ConfigManager;
import itz.silentcore.manager.FirstLaunchManager;
import itz.silentcore.manager.FriendManager;
import itz.silentcore.manager.WaypointManager;
import itz.silentcore.utils.client.Log;
import itz.silentcore.web.server.IRC;
import itz.silentcore.web.server.WeatherParser;
import itz.silentcore.manager.ModuleManager;
import itz.silentcore.utils.client.ClientUtility;
import itz.silentcore.web.utils.OpenLink;
import lombok.Getter;
import net.fabricmc.api.ModInitializer;
import ru.kotopushka.compiler.sdk.classes.Profile;

import static itz.silentcore.utils.client.IMinecraft.mc;

public class SilentCore implements ModInitializer {
    @Getter
    private static SilentCore instance;
    public ModuleManager moduleManager;
    public FirstLaunchManager firstLaunchManager;
    public ConfigManager configManager;
    public CommandManager commandManager;
    public AccountManager accountManager;
    public IRC ircManager;
    @Getter
    public FriendManager friendManager;
    @Getter
    public WaypointManager waypointManager;
    @Getter
    private String cachedWeather = "15"; // ето окак плейсхолдер
    public EventBus eventBus;
    public boolean enabled = true;

    public SilentCore() {
        instance = this;
    }

    @Override
    public void onInitialize() {
        eventBus = new EventBus();
        eventBus.register(this);

        moduleManager = new ModuleManager();
        firstLaunchManager = new FirstLaunchManager();
        eventBus.register(firstLaunchManager);

        configManager = new ConfigManager();
        accountManager = new AccountManager();
        waypointManager = new WaypointManager();
        ClientUtility.setCurrentTheme("CLIENT");
        commandManager = new CommandManager();
        new MediaPlayerRenderer();

        DiscordIntegration.initialize();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DiscordIntegration.shutdown();
        }, "Discord-Shutdown"));

        initializeNetworkComponents();
    }

    private void initializeNetworkComponents() {
        new Thread(() -> {
            try {
                ircManager = new IRC();
                friendManager = new FriendManager();
                friendManager.initialize();
                loadWeatherInBackground();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "Prank").start();
    }

    private void loadWeatherInBackground() {
        new Thread(() -> {
            try {
                cachedWeather = WeatherParser.weather();
            } catch (Exception e) {
                cachedWeather = "N/A";
            }
        }, "Weather-Init").start();
    }

    @Subscribe
    public void onTick(TickEvent tickEvent) {
        if (mc.options != null && mc.options.getMonochromeLogo() != null) {
            mc.options.getMonochromeLogo().setValue(true);
        }
    }
}