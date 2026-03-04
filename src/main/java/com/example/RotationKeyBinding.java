package com.example;

import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class RotationKeyBinding {
    public static final String KEYBINDING_ID = "example.rotation_keybinding";
    public static KeyBinding insertKeyBinding;

    public static void registerKeyBindings() {
        insertKeyBinding = FabricKeyBinding.Builder.create(
                new Identifier("example", "insert"),
                GLFW.GLFW_KEY_INSERT,
                "key.categories.gameplay"
        ).build();
        // Register the keybinding with Fabric API (placeholder method)
        FabricKeyBinding.register(insertKeyBinding);
    }
}