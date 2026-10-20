
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
import org.lwjgl.glfw.GLFW;

public class Freecam implements ClientModInitializer {
    private static KeyMapping openGuiKey;
    private static boolean enabled = false;
    private static double speed = 1.0;

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
        });
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
                bx + bw / 2,
                by + 10,
                TEXT
            );

            g.drawString(font, "Camera speed: " + speed + "x",
                bx, by + 42, TEXT);

            g.fill(bx, by + 59, bx + bw, by + 63, 0xFF624052);
            g.fill(bx, by + 59,
                bx + (int) (bw * speed / 5.0),
                by + 63, ACCENT);

            g.drawString(font, "Tap the bar to adjust speed",
                bx, by + 70, MUTED);

            g.drawCenteredString(font, "RIGHT SHIFT / ESC to close",
                width / 2, y + h - 17, MUTED);
        }

        @Override
        public boolean mouseClicked(
            MouseButtonEvent event, boolean doubleClick
        ) {
            double mouseX = event.x();
            double mouseY = event.y();

            if (event.button() == 0) {
                int w = Math.min(320, width - 20);
                int h = Math.min(190, height - 20);
                int x = (width - w) / 2;
                int y = (height - h) / 2;
                int bx = x + 14;
                int bw = w - 28;

                if (mouseX >= bx && mouseX <= bx + bw
                    && mouseY >= y + 55 && mouseY <= y + 83) {
                    enabled = !enabled;
                    return true;
                }

                if (mouseX >= bx && mouseX <= bx + bw
                    && mouseY >= y + 114 && mouseY <= y + 120) {
                    speed = Math.max(0.5, Math.min(5.0,
                        5.0 * (mouseX - bx) / bw));
                    speed = Math.round(speed * 10.0) / 10.0;
                    return true;
                }
            }

            return super.mouseClicked(event, doubleClick);
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            int keyCode = event.key();

            if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT
                || keyCode == GLFW.GLFW_KEY_ESCAPE) {
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

