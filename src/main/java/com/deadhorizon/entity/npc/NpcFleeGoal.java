package com.deadhorizon.entity.npc;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

/** Badly hurt humans break off and run until they have put some distance between them and the threat. */
public class NpcFleeGoal extends Goal {
    private final HumanNpcEntity mob;
    private Vec3d dest;

    public NpcFleeGoal(HumanNpcEntity mob) {
        this.mob = mob;
        setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        LivingEntity target = mob.getTarget();
        if (target == null || mob.getHealth() > mob.getMaxHealth() * 0.3f || mob.distanceTo(target) > 20) return false;
        dest = NoPenaltyTargeting.findFrom(mob, 16, 6, target.getPos());
        return dest != null;
    }

    @Override
    public void start() {
        mob.getNavigation().startMovingTo(dest.x, dest.y, dest.z, 1.35);
    }

    @Override
    public boolean shouldContinue() {
        return !mob.getNavigation().isIdle() && mob.getHealth() <= mob.getMaxHealth() * 0.3f;
    }
}
