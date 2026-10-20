package dev.fiesclient.freecam;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class FreecamCameraMixin {
    @Inject(method = "setup", at = @At("TAIL"))
    private void freecam$setup(
        net.minecraft.client.multiplayer.ClientLevel level,
        net.minecraft.world.entity.Entity entity,
        boolean detached,
        boolean thirdPerson,
        float partialTick,
        CallbackInfo ci
    ) {
        if (!FreecamController.isEnabled()) return;

        Camera camera = (Camera) (Object) this;
        camera.setPosition(FreecamController.getCameraPosition());
        camera.setRotation(
            FreecamController.getCameraYaw(),
            FreecamController.getCameraPitch()
        );
    }
}
