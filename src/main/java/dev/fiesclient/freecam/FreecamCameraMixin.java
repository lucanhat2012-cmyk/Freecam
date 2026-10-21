
package dev.fiesclient.freecam;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class FreecamCameraMixin {
    @Shadow
    protected abstract void setPosition(Vec3 position);

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "setup", at = @At("TAIL"))
    private void freecam$setup(
        ClientLevel level,
        Entity entity,
        boolean detached,
        boolean thirdPerson,
        float partialTick,
        CallbackInfo ci
    ) {
        if (!FreecamController.isEnabled()) return;

        setPosition(FreecamController.getCameraPosition());
        setRotation(
            FreecamController.getCameraYaw(),
            FreecamController.getCameraPitch()
        );
    }
}
