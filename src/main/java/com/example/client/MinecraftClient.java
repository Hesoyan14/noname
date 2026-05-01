package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinecraftClient implements ClientModInitializer {
	public static final String MOD_ID = "minecraftclient";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		LOGGER.info("Minecraft Client mod initialized!");
		
		// Здесь будет инициализация вашего клиента
	}
}
