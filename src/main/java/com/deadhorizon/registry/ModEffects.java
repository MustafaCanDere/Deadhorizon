package com.deadhorizon.registry;

import com.deadhorizon.DeadHorizon;
import com.deadhorizon.effect.BleedingEffect;
import com.deadhorizon.effect.InfectionEffect;
import com.deadhorizon.effect.SimpleEffect;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModEffects {
    public static final StatusEffect INFECTION = register("infection", new InfectionEffect());
    public static final StatusEffect BLEEDING = register("bleeding", new BleedingEffect());
    public static final StatusEffect IMMUNITY = register("immunity",
            new SimpleEffect(StatusEffectCategory.BENEFICIAL, 0x4FA3E0));
    /** A broken leg: limping, no sprinting (see Survival and the client sprint check). A splint fixes it. */
    public static final StatusEffect FRACTURE = register("fracture",
            new SimpleEffect(StatusEffectCategory.HARMFUL, 0xC8B89A).addAttributeModifier(
                    EntityAttributes.GENERIC_MOVEMENT_SPEED, "9b6c2a71-3f4e-4d2b-8a15-6e2f7c9d0b43", -0.35,
                    EntityAttributeModifier.Operation.MULTIPLY_TOTAL));

    private ModEffects() {
    }

    private static StatusEffect register(String name, StatusEffect effect) {
        return Registry.register(Registries.STATUS_EFFECT, DeadHorizon.id(name), effect);
    }

    public static void register() {
    }
}
