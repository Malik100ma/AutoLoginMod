package com.autologin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class AutoLoginScreen extends Screen {
	private final Screen parent;
	private final AutoLoginConfig cfg = AutoLoginConfig.get();

	private TextFieldWidget passwordField;
	private ButtonWidget toggleButton;

	public AutoLoginScreen(Screen parent) {
		super(Text.translatable("autologin.screen.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int cx = this.width / 2;
		int y = this.height / 2 - 45;

		toggleButton = addDrawableChild(ButtonWidget.builder(toggleText(), button -> {
			cfg.enabled = !cfg.enabled;
			button.setMessage(toggleText());
		}).dimensions(cx - 100, y, 200, 20).build());

		passwordField = new TextFieldWidget(this.textRenderer, cx - 100, y + 45, 200, 20,
				Text.translatable("autologin.screen.password"));
		passwordField.setMaxLength(128);
		passwordField.setText(cfg.password);
		addDrawableChild(passwordField);

		addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> this.close())
				.dimensions(cx - 100, y + 85, 200, 20).build());
	}

	private Text toggleText() {
		return Text.translatable("autologin.screen.enabled",
				cfg.enabled ? ScreenTexts.ON : ScreenTexts.OFF);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		int cx = this.width / 2;
		int y = this.height / 2 - 45;
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, cx, y - 25, 0xFFFFFFFF);
		context.drawTextWithShadow(this.textRenderer,
				Text.translatable("autologin.screen.password"), cx - 100, y + 33, 0xFFAAAAAA);
	}

	@Override
	public void removed() {
		cfg.password = passwordField.getText();
		AutoLoginConfig.save();
	}

	@Override
	public void close() {
		this.client.setScreen(parent);
	}
}
