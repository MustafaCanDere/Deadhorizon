package com.deadhorizon.mixin;

import com.deadhorizon.entity.zombie.BruteEntity;
import com.deadhorizon.entity.zombie.DHZombieEntity;
import com.deadhorizon.infection.Infection;
import com.deadhorizon.survival.Survival;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Every zombie hit on a player can infect or cause bleeding; ours can also grab hold. */
@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin {
    @Inject(method = "tryAttack", at = @At("RETURN"))
    private void deadhorizon$infectOnHit(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && target instanceof PlayerEntity player) {
            Infection.onZombieHit(player, (Object) this instanceof DHZombieEntity);
            if ((Object) this instanceof DHZombieEntity) Survival.onZombieGrab(player);
            if ((Object) this instanceof BruteEntity) Survival.onBruteHit(player);
        }
    }
}
