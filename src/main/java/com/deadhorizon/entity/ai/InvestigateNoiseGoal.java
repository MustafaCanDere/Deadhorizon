package com.deadhorizon.entity.ai;

import com.deadhorizon.entity.zombie.DHZombieEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;

import java.util.EnumSet;

/** Walks toward the last noise this zombie heard while it has nothing to attack. */
public class InvestigateNoiseGoal extends Goal {
    private static final int MAX_TICKS = 400;

    private final DHZombieEntity zombie;
    private final double speed;
    private int ticks;

    public InvestigateNoiseGoal(DHZombieEntity zombie, double speed) {
        this.zombie = zombie;
        this.speed = speed;
        this.setControls(EnumSet.of(Control.MOVE));
    }

    @Override
    public boolean canStart() {
        return zombie.getNoisePos() != null && zombie.getTarget() == null;
    }

    @Override
    public void start() {
        ticks = 0;
        moveToNoise();
    }

    @Override
    public boolean shouldContinue() {
        BlockPos pos = zombie.getNoisePos();
        return pos != null
                && zombie.getTarget() == null
                && ticks < MAX_TICKS
                && !pos.isWithinDistance(zombie.getPos(), 2.0);
    }

    @Override
    public void tick() {
        ticks++;
        // A newer noise may have replaced the old one, or the path may have ended early.
        if (ticks % 40 == 0 || zombie.getNavigation().isIdle()) {
            moveToNoise();
        }
    }

    @Override
    public void stop() {
        zombie.clearNoise();
        zombie.getNavigation().stop();
    }

    private void moveToNoise() {
        BlockPos pos = zombie.getNoisePos();
        if (pos != null) {
            zombie.getNavigation().startMovingTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, speed);
        }
    }
}
