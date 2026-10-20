package com.deadhorizon.effect;

import com.deadhorizon.registry.ModDamageTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/** Loses one heart-half every two seconds until bandaged or it runs out. */
public class BleedingEffect extends StatusEffect {
    public BleedingEffect() {
        super(StatusEffectCategory.HARMFUL, 0x8A0303);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration % 40 == 0;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (!entity.getWorld().isClient) {
            entity.damage(ModDamageTypes.of(entity.getWorld(), ModDamageTypes.BLEEDING), 1.0f + amplifier);
        }
    }
}
