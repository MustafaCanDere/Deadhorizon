package com.deadhorizon.mixin.client;

import com.deadhorizon.client.ClientSurvival;
import com.deadhorizon.registry.ModEffects;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** No sprinting while winded or on a broken leg. */
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    @Inject(method = "canSprint", at = @At("HEAD"), cancellable = true, require = 0)
    private void deadhorizon$canSprint(CallbackInfoReturnable<Boolean> cir) {
        if (ClientSurvival.blocksSprint((ClientPlayerEntity) (Object) this)) cir.setReturnValue(false);
    }
}
