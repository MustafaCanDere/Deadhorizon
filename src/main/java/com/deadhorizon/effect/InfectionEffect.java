package com.deadhorizon.effect;

import com.deadhorizon.infection.Infection;
import com.deadhorizon.registry.ModDamageTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * The zombie infection. Its remaining duration is the countdown: the first third is silent
 * incubation, the second brings fever (weakness, hunger), the last brings delirium (slowness,
 * nausea, damage). When it runs out the host dies and, if enabled, rises as a Walker.
 */
public class InfectionEffect extends StatusEffect {
    public InfectionEffect() {
        super(StatusEffectCategory.HARMFUL, 0x5E8C3A);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.getWorld().isClient) return;
        StatusEffectInstance instance = entity.getStatusEffect(this);
        if (instance == null) return;

        int left = instance.getDuration();
        int total = Infection.totalTicks();
        int feverAt = total * 2 / 3;
        int deliriumAt = total / 3;

        if (left == feverAt) {
            notify(entity, "message.deadhorizon.infection.fever", Formatting.YELLOW);
        } else if (left == deliriumAt) {
            notify(entity, "message.deadhorizon.infection.delirium", Formatting.RED);
        } else if (left == 20 * 60) {
            notify(entity, "message.deadhorizon.infection.final", Formatting.DARK_RED);
        }

        if (left <= feverAt && left % 100 == 0) {
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 140, 0, false, false, true));
            entity.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 140, 0, false, false, true));
        }
        if (left <= deliriumAt) {
            if (left % 100 == 0) {
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 140, 0, false, false, true));
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 140, 0, false, false, true));
            }
            if (left % 600 == 0) {
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0, false, false, true));
            }
            if (left % 300 == 0) {
                entity.damage(ModDamageTypes.of(entity.getWorld(), ModDamageTypes.INFECTION), 1.0f);
            }
        }
        if (left <= 1) {
            entity.damage(ModDamageTypes.of(entity.getWorld(), ModDamageTypes.INFECTION), Float.MAX_VALUE);
        }
    }

    private static void notify(LivingEntity entity, String key, Formatting color) {
        if (entity instanceof PlayerEntity player) {
            player.sendMessage(Text.translatable(key).formatted(color), true);
        }
    }
}
