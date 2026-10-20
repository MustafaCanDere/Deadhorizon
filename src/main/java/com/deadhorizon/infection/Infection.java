package com.deadhorizon.infection;

import com.deadhorizon.config.DHConfig;
import com.deadhorizon.entity.zombie.WalkerEntity;
import com.deadhorizon.registry.ModDamageTypes;
import com.deadhorizon.registry.ModEffects;
import com.deadhorizon.registry.ModEntities;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Rules for catching, slowing and curing the infection, plus turning dead hosts into Walkers. */
public final class Infection {
    private Infection() {
    }

    public static int totalTicks() {
        return Math.max(1, DHConfig.INSTANCE.infectionMinutes) * 20 * 60;
    }

    public static boolean isInfected(LivingEntity entity) {
        return entity.hasStatusEffect(ModEffects.INFECTION);
    }

    /** Called after a zombie lands a hit on a player. */
    public static void onZombieHit(PlayerEntity player, boolean ourZombie) {
        if (player.getWorld().isClient || player.isCreative() || player.isSpectator()) return;
        DHConfig cfg = DHConfig.INSTANCE;
        if (cfg.infectionEnabled && (ourZombie || cfg.vanillaZombiesInfect)
                && !isInfected(player) && !player.hasStatusEffect(ModEffects.IMMUNITY)
                && player.getRandom().nextDouble() < cfg.infectionChance) {
            infect(player);
        }
        if (cfg.bleedingEnabled && player.getRandom().nextDouble() < cfg.bleedChance) {
            player.addStatusEffect(new StatusEffectInstance(ModEffects.BLEEDING, cfg.bleedSeconds * 20));
        }
    }

    public static void infect(PlayerEntity player) {
        player.addStatusEffect(new StatusEffectInstance(ModEffects.INFECTION, totalTicks(), 0, false, false, true));
        player.sendMessage(Text.translatable("message.deadhorizon.infection.bitten").formatted(Formatting.DARK_GREEN), true);
    }

    /** Antibiotics push the countdown back, never beyond a fresh infection. */
    public static boolean slow(LivingEntity entity) {
        StatusEffectInstance current = entity.getStatusEffect(ModEffects.INFECTION);
        if (current == null) return false;
        int extended = Math.min(totalTicks(), current.getDuration() + DHConfig.INSTANCE.antibioticsDelayMinutes * 20 * 60);
        entity.removeStatusEffect(ModEffects.INFECTION);
        entity.addStatusEffect(new StatusEffectInstance(ModEffects.INFECTION, extended, 0, false, false, true));
        return true;
    }

    public static void cure(LivingEntity entity) {
        entity.removeStatusEffect(ModEffects.INFECTION);
        int immunity = DHConfig.INSTANCE.antidoteImmunityMinutes * 20 * 60;
        if (immunity > 0) {
            entity.addStatusEffect(new StatusEffectInstance(ModEffects.IMMUNITY, immunity));
        }
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity instanceof PlayerEntity player) || !source.isOf(ModDamageTypes.INFECTION)) return;
            if (!DHConfig.INSTANCE.infectedPlayersTurn || !(player.getWorld() instanceof ServerWorld world)) return;
            WalkerEntity walker = ModEntities.WALKER.create(world);
            if (walker == null) return;
            walker.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
            walker.setCustomName(player.getName().copy().formatted(Formatting.DARK_GREEN));
            walker.setPersistent();
            world.spawnEntity(walker);
        });
    }
}
