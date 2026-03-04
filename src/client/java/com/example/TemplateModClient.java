package com.example;

import net.fabricmc.api.ClientModInitializer;

public class TemplateModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		RotationHandler.register();
		// Example default target: look east and slightly down
		RotationHandler.setTarget(90f, -30f);
	}
}