package com.deadhorizon.mixin;

import com.deadhorizon.config.DHConfig;
import com.deadhorizon.entity.zombie.DHZombieEntity;
import com.deadhorizon.survival.ZombieSenses;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Scales how far our zombies can pick a player out, using {@link ZombieSenses}. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "getAttackDistanceScalingFactor", at = @At("RETURN"), cancellable = true)
    private void deadhorizon$senses(@Nullable Entity observer, CallbackInfoReturnable<Double> cir) {
        if (DHConfig.INSTANCE.zombieSensesEnabled && observer instanceof DHZombieEntity
                && (Object) this instanceof PlayerEntity player) {
            cir.setReturnValue(cir.getReturnValue() * ZombieSenses.visibility(player));
        }
    }
}
