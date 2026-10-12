package com.autologin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AutoLoginConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("autologin.json");

	private static AutoLoginConfig instance = new AutoLoginConfig();

	/** Включён ли мод. */
	public boolean enabled = true;
	/** Пароль, который подставляется в /login. */
	public String password = "";
	/** Задержка перед отправкой команды (в тиках, 20 тиков = 1 секунда). */
	public int delayTicks = 20;

	public static AutoLoginConfig get() {
		return instance;
	}

	public static void load() {
		if (Files.exists(PATH)) {
			try {
				AutoLoginConfig loaded = GSON.fromJson(Files.readString(PATH), AutoLoginConfig.class);
				if (loaded != null) {
					if (loaded.password == null) loaded.password = "";
					if (loaded.delayTicks < 0) loaded.delayTicks = 0;
					instance = loaded;
				}
			} catch (Exception e) {
				AutoLoginClient.LOGGER.error("Failed to read config, using defaults", e);
			}
		} else {
			save();
		}
	}

	public static void save() {
		try {
			Files.writeString(PATH, GSON.toJson(instance));
		} catch (IOException e) {
			AutoLoginClient.LOGGER.error("Failed to save config", e);
		}
	}
}
