package com.deadhorizon.survival;

import com.deadhorizon.config.DHConfig;
import com.deadhorizon.infection.Infection;
import com.deadhorizon.registry.ModEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

/**
 * How easy a player is for our zombies to notice, as a multiplier on their detection range.
 * Darkness hides you unless you carry a light; blood draws them in; the infected smell like one of them.
 */
public final class ZombieSenses {
    private ZombieSenses() {
    }

    public static double visibility(PlayerEntity player) {
        DHConfig cfg = DHConfig.INSTANCE;
        double factor = 1.0;
        if (holdsLight(player)) {
            factor *= 1.2;
        } else {
            int light = player.getWorld().getLightLevel(player.getBlockPos());
            if (light <= 4) factor *= cfg.darknessVisibility;
            else if (light <= 8) factor *= (1.0 + cfg.darknessVisibility) / 2.0;
        }
        if (player.hasStatusEffect(ModEffects.BLEEDING)) factor *= cfg.bleedingScentMultiplier;
        if (Infection.isInfected(player)) factor *= 0.85;
        return factor;
    }

    private static boolean holdsLight(PlayerEntity player) {
        return isLight(player.getMainHandStack()) || isLight(player.getOffHandStack());
    }

    private static boolean isLight(ItemStack stack) {
        return stack.getItem() instanceof BlockItem item && item.getBlock().getDefaultState().getLuminance() > 0;
    }
}
