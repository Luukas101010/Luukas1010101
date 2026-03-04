package com.example;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * Simplified smooth rotation/aim helper inspired by FarmHelper's rotation system.
 *
 * Press the toggle key to start easing the camera from the player's current rotation
 * to the configured target yaw/pitch over a fixed duration.
 */
public class RotationHandler {
    private static KeyMapping toggleKey;

    private static boolean rotating = false;
    private static float startYaw;
    private static float startPitch;
    private static float targetYaw;
    private static float targetPitch;

    private static long startTimeMs;
    private static long endTimeMs;

    // Simple movement automation state
    // After rotation, walk forward; when blocked, start strafing left/right alternating.
    private enum MoveState { ROTATE_TO_TARGET, MOVE_FORWARD, MOVE_LEFT, MOVE_RIGHT }
    private static boolean active = false;
    private static MoveState moveState = MoveState.ROTATE_TO_TARGET;
    private static double lastVelocity = 0;

    // Track which movement keys we are holding down
    private static boolean pressingForward = false;
    private static boolean pressingLeft = false;
    private static boolean pressingRight = false;

    public static void register() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.fh.rotation_toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            LocalPlayer player = client.player;
            if (player == null) return;

            if (toggleKey.consumeClick() && !(client.screen instanceof Screen)) {
                active = !active;
                if (active) {
                    moveState = MoveState.ROTATE_TO_TARGET;
                    // start rotation towards target
                    startYaw = player.getYRot();
                    startPitch = player.getXRot();
                    startTimeMs = System.currentTimeMillis();
                    endTimeMs = startTimeMs + 600L; // 600 ms rotation
                    rotating = true;
                } else {
                    rotating = false;
                    // Release any keys we may be holding
                    if (pressingForward) {
                        client.options.keyUp.setDown(false);
                        pressingForward = false;
                    }
                    if (pressingLeft) {
                        client.options.keyLeft.setDown(false);
                        pressingLeft = false;
                    }
                    if (pressingRight) {
                        client.options.keyRight.setDown(false);
                        pressingRight = false;
                    }
                }
            }

            if (!active) return;

            if (moveState == MoveState.ROTATE_TO_TARGET) {
                if (!rotating) {
                    // rotation finished, start by walking forward
                    moveState = MoveState.MOVE_FORWARD;
                } else {
                    long now = System.currentTimeMillis();
                    if (now >= endTimeMs) {
                        player.setYRot(targetYaw);
                        player.setXRot(targetPitch);
                        rotating = false;
                    } else {
                        float t = (now - startTimeMs) / (float) (endTimeMs - startTimeMs);
                        t = easeOutExpo(t);

                        float newYaw = lerp(startYaw, targetYaw, t);
                        float newPitch = lerp(startPitch, targetPitch, t);

                        player.setYRot(newYaw);
                        player.setXRot(newPitch);
                    }
                    return;
                }
            }

            // Movement phase
            float strafe = 0f;

            switch (moveState) {
                case MOVE_FORWARD -> { /* only W, handled below */ }
                case MOVE_LEFT -> strafe = -1f;
                case MOVE_RIGHT -> strafe = 1f;
                default -> { }
            }

            // Simulate key presses instead of directly changing velocity
            // Always keep W (forward) held while the automation is active.
            if (!pressingForward) {
                client.options.keyUp.setDown(true);
                pressingForward = true;
            }

            // Add A (left) / D (right) while strafing.
            if (strafe < 0) {
                client.options.keyLeft.setDown(true);
                pressingLeft = true;
                if (pressingRight) {
                    client.options.keyRight.setDown(false);
                    pressingRight = false;
                }
            } else if (strafe > 0) {
                client.options.keyRight.setDown(true);
                pressingRight = true;
                if (pressingLeft) {
                    client.options.keyLeft.setDown(false);
                    pressingLeft = false;
                }
            } else {
                if (pressingLeft) {
                    client.options.keyLeft.setDown(false);
                    pressingLeft = false;
                }
                if (pressingRight) {
                    client.options.keyRight.setDown(false);
                    pressingRight = false;
                }
            }

            // Use actual horizontal movement speed to detect when we are blocked.
            double velocity = Math.sqrt(
                    player.getDeltaMovement().x * player.getDeltaMovement().x +
                            player.getDeltaMovement().z * player.getDeltaMovement().z
            );

            if (velocity < 0.01 && lastVelocity > 0.01) {
                // Speed dropped: switch to the opposite horizontal direction
                moveState = switch (moveState) {
                    case MOVE_FORWARD -> MoveState.MOVE_LEFT;
                    case MOVE_LEFT -> MoveState.MOVE_RIGHT;
                    case MOVE_RIGHT -> MoveState.MOVE_LEFT;
                    default -> MoveState.MOVE_FORWARD;
                };
            }

            lastVelocity = velocity;
        });
    }

    public static void setTarget(float yaw, float pitch) {
        targetYaw = normalizeYaw(yaw);
        targetPitch = pitch;
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    // Approximation of FarmHelper's easeOutExpo for a smooth, quick finish.
    private static float easeOutExpo(float x) {
        if (x >= 1f) return 1f;
        return (float) (1 - Math.pow(2, -10 * x));
    }

    private static float normalizeYaw(float yaw) {
        float newYaw = yaw % 360F;
        if (newYaw < -180F) {
            newYaw += 360F;
        }
        if (newYaw > 180F) {
            newYaw -= 360F;
        }
        return newYaw;
    }
}
