
package dev.fiesclient.freecam;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public final class FreecamController {
    private static boolean enabled = false;
    private static double speed = 1.0;
    private static double x, y, z;
    private static float yaw, pitch;

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

    public static Vec3 getCameraPosition() {
        return new Vec3(x, y, z);
    }

    public static float getCameraYaw() {
        return yaw;
    }

    public static float getCameraPitch() {
        return pitch;
    }

    public static void rotateCamera(double dx, double dy) {
        if (!enabled) return;
        yaw += (float) dx * 0.15f;
        pitch = Math.max(-90.0f, Math.min(90.0f,
            pitch + (float) dy * 0.15f));
    }

    public static void toggle(Minecraft client) {
        if (enabled) {
            enabled = false;
            if (client.player != null) {
                client.setCameraEntity(client.player);
            }
            return;
        }

        if (client.player == null || client.level == null) return;

        x = client.player.getX();
        y = client.player.getEyeY();
        z = client.player.getZ();
        yaw = client.player.getYRot();
        pitch = client.player.getXRot();
        enabled = true;
    }

    public static void tick(Minecraft client) {
        if (!enabled) return;

        if (client.player == null || client.level == null) {
            enabled = false;
            return;
        }

        double r = Math.toRadians(yaw);
        double fx = -Math.sin(r);
        double fz = Math.cos(r);
        double rx = Math.cos(r);
        double rz = Math.sin(r);

        double dx = 0, dy = 0, dz = 0;

        if (client.options.keyUp.isDown()) {
            dx += fx;
            dz += fz;
        }
        if (client.options.keyDown.isDown()) {
            dx -= fx;
            dz -= fz;
        }
        if (client.options.keyRight.isDown()) {
            dx += rx;
            dz += rz;
        }
        if (client.options.keyLeft.isDown()) {
            dx -= rx;
            dz -= rz;
        }
        if (client.options.keyJump.isDown()) dy++;
        if (client.options.keyShift.isDown()) dy--;

        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length > 0) {
            double amount = 0.25 * speed;
            if (client.options.keySprint.isDown()) amount *= 2;
            x += dx / length * amount;
            y += dy / length * amount;
            z += dz / length * amount;
        }
    }
}
