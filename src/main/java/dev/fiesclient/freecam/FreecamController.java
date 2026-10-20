
package dev.fiesclient.freecam;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class FreecamController {
    private static boolean enabled = false;
    private static double speed = 1.0;

    private static Entity cameraEntity;
    private static double cameraX;
    private static double cameraY;
    private static double cameraZ;
    private static float cameraYaw;
    private static float cameraPitch;

    private FreecamController() {}

    public static boolean isEnabled() {
        return enabled;
    }

    public static double getSpeed() {
        return speed;
    }

    public static void setSpeed(double value) {
        speed = Math.max(0.5, Math.min(5.0, value));
    }

    public static void toggle(Minecraft client) {
        if (enabled) {
            stop(client);
        } else {
            start(client);
        }
    }

    private static void start(Minecraft client) {
        if (client.player == null || client.level == null) {
            return;
        }

        cameraEntity = client.player;
        cameraX = client.player.getX();
        cameraY = client.player.getEyeY();
        cameraZ = client.player.getZ();
        cameraYaw = client.player.getYRot();
        cameraPitch = client.player.getXRot();

        enabled = true;
    }

    private static void stop(Minecraft client) {
        enabled = false;

        if (client.player != null) {
            client.setCameraEntity(client.player);
        }

        cameraEntity = null;
    }

    public static void tick(Minecraft client) {
        if (!enabled) return;

        if (client.player == null || client.level == null) {
            stop(client);
            return;
        }

        // Keep the detached camera's position separate from the player.
        float yaw = cameraYaw;
        double radians = Math.toRadians(yaw);

        double forwardX = -Math.sin(radians);
        double forwardZ = Math.cos(radians);
        double rightX = Math.cos(radians);
        double rightZ = Math.sin(radians);

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

            cameraX += dx / length * amount;
            cameraY += dy / length * amount;
            cameraZ += dz / length * amount;
        }

        // Mouse-look and a separate camera entity will be added next.
    }

    public static Vec3 getCameraPosition() {
        return new Vec3(cameraX, cameraY, cameraZ);
    }

    public static float getCameraYaw() {
        return cameraYaw;
    }

    public static float getCameraPitch() {
        return cameraPitch;
    }

    public static void rotateCamera(double deltaX, double deltaY) {
        if (!enabled) return;

        cameraYaw += (float) deltaX * 0.15f;
        cameraPitch += (float) deltaY * 0.15f;
        cameraPitch = Math.max(-90.0f, Math.min(90.0f, cameraPitch));
    }
}
