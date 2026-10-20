
package dev.fiesclient.freecam;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.lwjgl.glfw.GLFW;

public class Freecam implements ClientModInitializer {
    private static KeyMapping openGuiKey;
    private static boolean enabled = false;
    private static double speed = 1.0;
    private static ArmorStand cameraEntity;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("freecam", "controls")
        );

        openGuiKey = KeyBindingHelper.registerKeyBinding(
            new KeyMapping(
                "key.freecam.open_gui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                category
            )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.consumeClick()) {
                if (client.player != null) {
                    client.setScreen(new FreecamScreen());
                }
            }

            if (!enabled) return;

            if (client.player == null || client.level == null
                || cameraEntity == null) {
                stopFreecam(client);
                return;
            }

            double yaw = Math.toRadians(cameraEntity.getYRot());
            double forwardX = -Math.sin(yaw);
            double forwardZ = Math.cos(yaw);
            double rightX = Math.cos(yaw);
            double rightZ = Math.sin(yaw);

            double dx = 0;
            double dy = 0;
            double dz = 0;

            if (client.options.keyUp.isDown()) {
                dx += forwardX;
                dz += forwardZ;
            }
            if (client.options.keyDown.isDown()) {
                dx -= forwardX;
                dz -= forwardZ;
            }
            if (client.options.keyRight.isDown()) {
                dx += rightX;
                dz += rightZ;
            }
            if (client.options.keyLeft.isDown()) {
                dx -= rightX;
                dz -= rightZ;
            }
            if (client.options.keyJump.isDown()) dy += 1;
            if (client.options.keyShift.isDown()) dy -= 1;

            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (length > 0) {
                double amount = 0.25 * speed;
                if (client.options.keySprint.isDown()) {
                    amount *= 2;
                }

                cameraEntity.setPos(
                    cameraEntity.getX() + dx / length * amount,
                    cameraEntity.getY() + dy / length * amount,
                    cameraEntity.getZ() + dz / length * amount
                );
            }
        });
    }

    private static void startFreecam(Minecraft client) {
        if (client.player == null || client.level == null) return;

        cameraEntity = new ArmorStand(
            EntityType.ARMOR_STAND, client.level
        );

        cameraEntity.setPos(
            client.player.getX(),
            client.player.getY(),
            client.player.getZ()
        );
        cameraEntity.setYRot(client.player.getYRot());
        cameraEntity.setXRot(client.player.getXRot());
        cameraEntity.setNoGravity(true);
        cameraEntity.setInvisible(true);

        enabled = true;
        client.setCameraEntity(cameraEntity);
    }

    private static void stopFreecam(Minecraft client) {
        enabled = false;
        cameraEntity = null;

        if (client.player != null) {
            client.setCameraEntity(client.player);
        }
    }

    private static void toggleFreecam(Minecraft client) {
        if (enabled) {
            stopFreecam(client);
        } else {
            startFreecam(client);
        }
    }

    public static class FreecamScreen extends Screen {
        private static final int BG = 0xE9160D16;
        private static final int PANEL = 0xFF281725;
        private static final int ACCENT = 0xFFFF83C8;
        private static final int TEXT = 0xFFFFEAF6;
        private static final int MUTED = 0xFFBFA8BA;

        protected FreecamScreen() {
            super(Component.literal("Freecam"));
        }

        @Override
        public void render(
            GuiGraphics g, int mouseX, int mouseY, float delta
        ) {
            g.fill(0, 0, width, height, BG);

            int w = Math.min(320, width - 20);
            int h = Math.min(190, height - 20);
            int x = (width - w) / 2;
            int y = (height - h) / 2;

            g.fill(x, y, x + w, y + h, PANEL);
            g.fill(x, y, x + w, y + 3, ACCENT);

            g.drawString(font, "Freecam", x + 14, y + 14, ACCENT);
            g.drawString(font, "Made by FiesClient",
                x + 14, y + 29, MUTED);

            int bx = x + 14;
            int by = y + 55;
            int bw = w - 28;

            g.fill(bx, by, bx + bw, by + 28,
                enabled ? 0xFF63304F : 0xFF392438);

            g.drawCenteredString(
                font,
                enabled ? "Freecam: ON" : "Freecam: OFF",
                bx + bw / 2, by + 10, TEXT
            );

            g.drawString(font, "Camera speed: " + speed + "x",
                bx, by + 42, TEXT);

            g.fill(bx, by + 59, bx + bw, by + 63, 0xFF624052);
            g.fill(bx, by + 59,
                bx + (int) (bw * speed / 5.0),
                by + 63, ACCENT);

            g.drawString(font, "WASD move, Space/Shift up/down",
                bx, by + 70, MUTED);

            g.drawCenteredString(font, "RIGHT SHIFT / ESC to close",
                width / 2, y + h - 17, MUTED);
        }

        @Override
        public boolean mouseClicked(
            MouseButtonEvent event, boolean doubleClick
        ) {
            double mx = event.x();
            double my = event.y();

            if (event.button() == 0) {
                int w = Math.min(320, width - 20);
                int h = Math.min(190, height - 20);
                int x = (width - w) / 2;
                int y = (height - h) / 2;
                int bx = x + 14;
                int bw = w - 28;

                if (mx >= bx && mx <= bx + bw
                    && my >= y + 55 && my <= y + 83) {
                    toggleFreecam(Minecraft.getInstance());
                    return true;
                }

                if (mx >= bx && mx <= bx + bw
                    && my >= y + 114 && my <= y + 120) {
                    speed = Math.max(0.5, Math.min(5.0,
                        5.0 * (mx - bx) / bw));
                    speed = Math.round(speed * 10) / 10.0;
                    return true;
                }
            }

            return super.mouseClicked(event, doubleClick);
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            int key = event.key();

            if (key == GLFW.GLFW_KEY_RIGHT_SHIFT
                || key == GLFW.GLFW_KEY_ESCAPE) {
                onClose();
                return true;
            }

            return super.keyPressed(event);
        }

        @Override
        public void onClose() {
            Minecraft.getInstance().setScreen(null);
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }
}
