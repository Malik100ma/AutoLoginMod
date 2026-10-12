package com.autologin;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoLoginClient implements ClientModInitializer {
	public static final String MOD_ID = "autologin";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final KeyBinding.Category CATEGORY =
			KeyBinding.Category.create(Identifier.of(MOD_ID, "main"));

	private static KeyBinding openSettingsKey;
	private static KeyBinding toggleKey;

	/** Тиков до отправки /login; -1 = ничего не ожидается. */
	private int pendingTicks = -1;

	@Override
	public void onInitializeClient() {
		AutoLoginConfig.load();

		// Открыть настройки (по умолчанию: Right Shift)
		openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.autologin.open_settings",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				CATEGORY));

		// Быстро вкл/выкл мод (по умолчанию не назначен)
		toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.autologin.toggle",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_UNKNOWN,
				CATEGORY));

		// Игрок зашёл на сервер -> запланировать /login
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			AutoLoginConfig cfg = AutoLoginConfig.get();
			if (!cfg.enabled || cfg.password.isEmpty() || client.isInSingleplayer()) {
				pendingTicks = -1;
				return;
			}
			pendingTicks = Math.max(1, cfg.delayTicks);
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> pendingTicks = -1);

		ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
	}

	private void onTick(MinecraftClient client) {
		while (openSettingsKey.wasPressed()) {
			client.setScreen(new AutoLoginScreen(client.currentScreen));
		}

		while (toggleKey.wasPressed()) {
			AutoLoginConfig cfg = AutoLoginConfig.get();
			cfg.enabled = !cfg.enabled;
			AutoLoginConfig.save();
			if (client.player != null) {
				client.player.sendMessage(Text.translatable(
						cfg.enabled ? "autologin.message.enabled" : "autologin.message.disabled"), true);
			}
		}

		if (pendingTicks >= 0) {
			if (client.player == null || client.getNetworkHandler() == null) {
				return;
			}
			if (--pendingTicks == 0) {
				pendingTicks = -1;
				AutoLoginConfig cfg = AutoLoginConfig.get();
				if (cfg.enabled && !cfg.password.isEmpty()) {
					client.getNetworkHandler().sendChatCommand("login " + cfg.password);
					client.player.sendMessage(Text.translatable("autologin.message.sent"), true);
				}
			}
		}
	}
}
