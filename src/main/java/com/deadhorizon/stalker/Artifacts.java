package com.deadhorizon.stalker;

import com.deadhorizon.registry.ModEffects;
import com.deadhorizon.survival.SurvivalData;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/** Artifacts work while carried on the belt (the hotbar or off hand); every one of them is mildly radioactive. */
public final class Artifacts {
    private static final float RADIATION_EACH = 0.25f;

    private Artifacts() {
    }

    /** Applies one second of artifact effects and returns the radiation they give off. */
    public static float tick(ServerPlayerEntity player, SurvivalData data) {
        float rad = 0.0f;
        for (int slot = 0; slot < 10; slot++) {
            ItemStack stack = slot == 9 ? player.getOffHandStack() : player.getInventory().getStack(slot);
            if (!(stack.getItem() instanceof ArtifactItem artifact)) continue;
            rad += RADIATION_EACH;
            switch (artifact.kind()) {
                case JELLYFISH -> {
                    if (player.getHealth() < player.getMaxHealth() && player.age % 60 == 0) player.heal(1.0f);
                    player.removeStatusEffect(ModEffects.BLEEDING);
                }
                case STONE_FLOWER -> data.addStamina(6.0f);
                case FIREBALL -> {
                    if (data.temperature < 70.0f) data.temperature = Math.min(70.0f, data.temperature + 2.0f);
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 40, 0, true, false, true));
                }
            }
        }
        return rad;
    }
}
