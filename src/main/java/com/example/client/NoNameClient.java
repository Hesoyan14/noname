package com.example.client;

import com.example.client.manager.ModuleManager;
import com.google.common.eventbus.EventBus;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoNameClient implements ClientModInitializer {
    public static final String MOD_ID = "noname";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static NoNameClient instance;

    private EventBus eventBus;
    private ModuleManager moduleManager;

    @Override
    public void onInitializeClient() {
        instance = this;

        eventBus = new EventBus();
        moduleManager = new ModuleManager();

        LOGGER.info("NoName client initialized!");
    }

    public static NoNameClient getInstance() { return instance; }
    public EventBus getEventBus() { return eventBus; }
    public ModuleManager getModuleManager() { return moduleManager; }
}
