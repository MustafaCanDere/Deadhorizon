package com.deadhorizon.combat;

import com.deadhorizon.DeadHorizon;
import com.deadhorizon.entity.zombie.CrawlerEntity;
import com.deadhorizon.survival.Survival;
import com.deadhorizon.survival.SurvivalData;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * Close-quarters moves on top of vanilla melee: a kick that shoves and staggers (or, sneaking, a stomp that
 * finishes a crawler on the ground), a stamina-paid dodge and a ledge vault. Movement itself happens on the
 * client; the server charges stamina and resolves the kick.
 */
public final class Combat {
    public static final Identifier ACTION = DeadHorizon.id("combat_action");
    public static final int KICK = 0, DODGE = 1, VAULT = 2;
    public static final float KICK_COST = 15.0f, DODGE_COST = 20.0f, VAULT_COST = 10.0f;
    private static final double KICK_REACH = 2.6;

    private Combat() {
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ACTION, (server, player, handler, buf, sender) -> {
            int action = buf.readByte();
            server.execute(() -> handle(player, action));
        });
    }

    public static boolean canAfford(SurvivalData data, float cost) {
        return !data.exhausted && data.stamina >= cost;
    }

    private static void handle(ServerPlayerEntity player, int action) {
        if (!player.isAlive() || player.isSpectator()) return;
        SurvivalData data = Survival.get(player);
        switch (action) {
            case KICK -> kick(player, data);
            case DODGE -> {
                if (!player.isCreative()) Survival.exert(player, DODGE_COST);
                player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE,
                        SoundCategory.PLAYERS, 0.6f, 0.7f);
            }
            case VAULT -> {
                if (!player.isCreative()) Survival.exert(player, VAULT_COST);
                player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_WOOL_STEP,
                        SoundCategory.PLAYERS, 0.8f, 0.8f);
            }
            default -> {
            }
        }
    }

    private static void kick(ServerPlayerEntity player, SurvivalData data) {
        if (!player.isCreative() && !canAfford(data, KICK_COST)) return;
        if (!player.isCreative()) Survival.exert(player, KICK_COST);
        player.swingHand(Hand.OFF_HAND, true);

        Vec3d eye = player.getEyePos();
        Vec3d look = player.getRotationVec(1.0f);
        Vec3d end = eye.add(look.multiply(KICK_REACH));
        Box sweep = player.getBoundingBox().stretch(look.multiply(KICK_REACH)).expand(1.0);
        EntityHitResult hit = ProjectileUtil.raycast(player, eye, end, sweep,
                e -> e instanceof LivingEntity && e.isAlive() && e.canHit(), KICK_REACH * KICK_REACH);
        if (hit == null) {
            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK,
                    SoundCategory.PLAYERS, 0.6f, 0.8f);
            return;
        }
        Entity target = hit.getEntity();
        LivingEntity living = (LivingEntity) target;
        boolean stomp = player.isSneaking() && (living instanceof CrawlerEntity || living.getY() < player.getY() - 0.4);
        if (stomp) {
            // Project Zomboid's stomp: a zombie on the ground gets its head caved in.
            living.damage(player.getDamageSources().playerAttack(player), 10.0f);
            player.getWorld().playSound(null, target.getBlockPos(), SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR,
                    SoundCategory.PLAYERS, 0.7f, 1.4f);
            return;
        }
        living.damage(player.getDamageSources().playerAttack(player), 2.0f);
        living.takeKnockback(1.4, player.getX() - living.getX(), player.getZ() - living.getZ());
        living.velocityModified = true;
        // Stagger: a kicked zombie reels back and can't hit for a moment.
        living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 30, 3, false, false));
        living.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 30, 4, false, false));
        player.getWorld().playSound(null, target.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK,
                SoundCategory.PLAYERS, 1.0f, 0.8f);
    }
}
