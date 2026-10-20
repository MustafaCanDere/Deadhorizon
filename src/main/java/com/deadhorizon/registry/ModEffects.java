package com.deadhorizon.registry;

import com.deadhorizon.DeadHorizon;
import com.deadhorizon.effect.BleedingEffect;
import com.deadhorizon.effect.InfectionEffect;
import com.deadhorizon.effect.SimpleEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModEffects {
    public static final StatusEffect INFECTION = register("infection", new InfectionEffect());
    public static final StatusEffect BLEEDING = register("bleeding", new BleedingEffect());
    public static final StatusEffect IMMUNITY = register("immunity",
            new SimpleEffect(StatusEffectCategory.BENEFICIAL, 0x4FA3E0));

    private ModEffects() {
    }

    private static StatusEffect register(String name, StatusEffect effect) {
        return Registry.register(Registries.STATUS_EFFECT, DeadHorizon.id(name), effect);
    }

    public static void register() {
    }
}
