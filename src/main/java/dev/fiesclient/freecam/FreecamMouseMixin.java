
package dev.fiesclient.freecam;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class FreecamMouseMixin {
    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void freecam$turnPlayer(CallbackInfo ci) {
        if (!FreecamController.isEnabled()) return;

        // Mouse-look will be connected after verifying
        // the exact 1.21.11 mouse method signature.
    }
}
