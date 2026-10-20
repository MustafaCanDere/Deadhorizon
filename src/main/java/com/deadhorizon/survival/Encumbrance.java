package com.deadhorizon.survival;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

/** A rough carried weight: a full stack of blocks counts 1, a full stack of anything else 0.5. */
public final class Encumbrance {
    public static final double HEAVY = 18.0;
    public static final double OVERLOADED = 26.0;

    private Encumbrance() {
    }

    public static double weight(PlayerEntity player) {
        double weight = 0.0;
        for (ItemStack stack : player.getInventory().main) {
            if (stack.isEmpty()) continue;
            double fill = (double) stack.getCount() / stack.getMaxCount();
            weight += fill * (stack.getItem() instanceof BlockItem ? 1.0 : 0.5);
        }
        return weight;
    }

    public static int load(PlayerEntity player) {
        double weight = weight(player);
        return weight >= OVERLOADED ? 2 : weight >= HEAVY ? 1 : 0;
    }
}
