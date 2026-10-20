package com.deadhorizon.stalker;

import com.deadhorizon.block.RadioactiveBlock;
import com.deadhorizon.config.DHConfig;
import com.deadhorizon.registry.ModDamageTypes;
import com.deadhorizon.survival.Survival;
import com.deadhorizon.survival.SurvivalData;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

/**
 * Radiation dose, 0-100. Contaminated blocks and carried artifacts add to it; it decays slowly on its own,
 * and anti-rad or vodka bring it down. Above 30 you feel weak, above 60 it starts to burn, above 85 it kills.
 */
public final class Radiation {
    public static final float MAX = 100.0f;
    private static final int SCAN_RADIUS = 6;

    private Radiation() {
    }

    /** Called once a second from the survival tick. */
    public static void tick(ServerPlayerEntity player, SurvivalData data) {
        DHConfig cfg = DHConfig.INSTANCE;
        float exposure = environment(player) + Artifacts.tick(player, data);
        exposure *= protection(player);
        data.exposure = exposure;
        float before = data.radiation;
        data.radiation = Math.max(0.0f, Math.min(MAX, data.radiation + exposure - (float) cfg.radiationDecayPerSecond));

        if (before < 30.0f && data.radiation >= 30.0f) {
            player.sendMessage(Text.translatable("message.deadhorizon.radiation_sick").formatted(Formatting.GOLD), true);
        }
        if (data.radiation >= 30.0f) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 0, true, false, true));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 60, 0, true, false, true));
        }
        if (data.radiation >= 60.0f && player.age % 100 == 0) {
            player.damage(ModDamageTypes.of(player.getWorld(), ModDamageTypes.RADIATION), 1.0f);
            if (player.getRandom().nextFloat() < 0.3f) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 120, 0, true, false, true));
            }
        }
        if (data.radiation >= 85.0f && player.age % 40 == 0) {
            player.damage(ModDamageTypes.of(player.getWorld(), ModDamageTypes.RADIATION), 1.5f);
        }
    }

    /** rad/s from contaminated blocks nearby, falling off with distance. */
    private static float environment(ServerPlayerEntity player) {
        BlockPos center = player.getBlockPos();
        float total = 0.0f;
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dy = -SCAN_RADIUS; dy <= SCAN_RADIUS; dy++) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                    pos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockState state = player.getWorld().getBlockState(pos);
                    if (state.getBlock() instanceof RadioactiveBlock source) {
                        total += source.strength() / (1.0f + 0.35f * (dx * dx + dy * dy + dz * dz));
                    }
                }
            }
        }
        return total;
    }

    /** Fraction of the dose that gets through: each worn hazmat piece blocks a share of it. */
    private static float protection(PlayerEntity player) {
        float factor = 1.0f;
        for (ItemStack armor : player.getArmorItems()) {
            if (armor.getItem() instanceof HazmatItem) factor -= 0.22f;
        }
        return Math.max(0.1f, factor);
    }

    public static void reduce(PlayerEntity player, float amount) {
        SurvivalData data = Survival.get(player);
        data.radiation = Math.max(0.0f, data.radiation - amount);
    }
}
