package com.deadhorizon.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

/** A marker effect with no tick behaviour of its own (StatusEffect's constructor is protected). */
public class SimpleEffect extends StatusEffect {
    public SimpleEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }
}
