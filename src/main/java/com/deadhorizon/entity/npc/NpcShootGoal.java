package com.deadhorizon.entity.npc;

import com.deadhorizon.weapon.GunItem;
import com.deadhorizon.weapon.GunSpec;
import com.deadhorizon.weapon.Guns;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

/** Keeps a fighting distance, fires aimed shots or short bursts, and stops to reload when the magazine runs dry. */
public class NpcShootGoal extends Goal {
    private final HumanNpcEntity mob;
    private int cooldown;
    private int burst;
    private int reloading;
    private int seeTicks;

    public NpcShootGoal(HumanNpcEntity mob) {
        this.mob = mob;
        setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    private GunItem gun() {
        return mob.getMainHandStack().getItem() instanceof GunItem g ? g : null;
    }

    @Override
    public boolean canStart() {
        LivingEntity target = mob.getTarget();
        return gun() != null && target != null && target.isAlive();
    }

    @Override
    public boolean shouldContinue() {
        return canStart();
    }

    @Override
    public void start() {
        cooldown = 10 + mob.getRandom().nextInt(15); // reaction time
    }

    @Override
    public void stop() {
        mob.setAttacking(false);
        mob.getNavigation().stop();
    }

    @Override
    public boolean shouldRunEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        GunItem gun = gun();
        if (target == null || gun == null) return;
        GunSpec spec = gun.spec();
        ItemStack stack = mob.getMainHandStack();
        mob.getLookControl().lookAt(target, 30.0f, 30.0f);
        mob.setAttacking(true);

        boolean sees = mob.getVisibilityCache().canSee(target);
        seeTicks = sees ? seeTicks + 1 : 0;
        double dist = mob.distanceTo(target);
        double ideal = Math.min(spec.range() * 0.5, spec.pellets() > 1 ? 7.0 : 16.0);

        if (!sees || dist > ideal + 4) {
            mob.getNavigation().startMovingTo(target, 1.0);
        } else if (dist < Math.min(ideal * 0.5, 6.0)) {
            Vec3d away = NoPenaltyTargeting.findFrom(mob, 8, 4, target.getPos());
            if (away != null) mob.getNavigation().startMovingTo(away.x, away.y, away.z, 1.15);
        } else {
            mob.getNavigation().stop();
        }

        if (reloading > 0) {
            if (--reloading == 0) mob.rounds = gun.magazine(stack);
            return;
        }
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (!sees || seeTicks < 5 || dist > spec.range()) return;

        fire(target, spec);
        mob.rounds--;
        if (mob.rounds <= 0) {
            reloading = spec.reloadTicks() + 20;
            mob.getWorld().playSound(null, mob.getBlockPos(), SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.HOSTILE, 0.6f, 1.6f);
            burst = 0;
            return;
        }
        if (spec.automatic()) {
            if (burst <= 0) burst = 2 + mob.getRandom().nextInt(4);
            cooldown = --burst > 0 ? spec.interval() : 20 + mob.getRandom().nextInt(25);
        } else {
            cooldown = Math.max(spec.interval() * 2, 16) + mob.getRandom().nextInt(16);
        }
    }

    private void fire(LivingEntity target, GunSpec spec) {
        if (!(mob.getWorld() instanceof ServerWorld world)) return;
        Vec3d eye = mob.getEyePos();
        Vec3d aim = target.getPos().add(0, target.getHeight() * 0.6, 0);
        Vec3d dir = aim.subtract(eye).normalize();
        // Humans are worse shots than the player's crosshair, and worse still at range or on the move.
        double spread = Math.toRadians(spec.spread() * 1.5 + 1.5 + mob.distanceTo(target) * 0.08
                + (target.getVelocity().horizontalLengthSquared() > 0.01 ? 2.0 : 0.0));
        for (int i = 0; i < spec.pellets(); i++) {
            Vec3d d = dir.add(mob.getRandom().nextGaussian() * spread, mob.getRandom().nextGaussian() * spread,
                    mob.getRandom().nextGaussian() * spread).normalize();
            Guns.shoot(mob, world, eye, d, spec, 0.6f);
        }
        Guns.muzzle(world, mob, spec, false);
    }
}
