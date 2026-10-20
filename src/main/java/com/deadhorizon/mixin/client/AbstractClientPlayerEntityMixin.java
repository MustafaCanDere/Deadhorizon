package com.deadhorizon.mixin.client;

import com.deadhorizon.weapon.GunItem;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Aiming: sneaking with a gun in hand zooms in by the gun's zoom factor. */
@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin {
    @Inject(method = "getFovMultiplier", at = @At("RETURN"), cancellable = true, require = 0)
    private void deadhorizon$aim(CallbackInfoReturnable<Float> cir) {
        AbstractClientPlayerEntity self = (AbstractClientPlayerEntity) (Object) this;
        if (self.isSneaking() && self.getMainHandStack().getItem() instanceof GunItem gun) {
            cir.setReturnValue(cir.getReturnValue() * gun.spec().zoom());
        }
    }
}
