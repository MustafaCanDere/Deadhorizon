package com.deadhorizon.entity.ai;

import com.deadhorizon.block.BarricadeBlock;
import com.deadhorizon.entity.zombie.DHZombieEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/** When a zombie has a target it can't reach and is standing at a barricade, it batters it down. */
public class BreakBarricadeGoal extends Goal {
    private final DHZombieEntity zombie;
    @Nullable
    private BlockPos barricade;
    private int ticks;

    public BreakBarricadeGoal(DHZombieEntity zombie) {
        this.zombie = zombie;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        LivingEntity target = zombie.getTarget();
        if (target == null || zombie.age % 10 != 0 || zombie.squaredDistanceTo(target) < 4.0) return false;
        barricade = findBarricade();
        return barricade != null;
    }

    @Override
    public boolean shouldContinue() {
        return barricade != null
                && zombie.getTarget() != null
                && ticks < 600
                && zombie.getWorld().getBlockState(barricade).getBlock() instanceof BarricadeBlock
                && barricade.isWithinDistance(zombie.getPos(), 2.5);
    }

    @Override
    public void start() {
        ticks = 0;
        zombie.getNavigation().stop();
    }

    @Override
    public void stop() {
        barricade = null;
    }

    @Override
    public void tick() {
        if (barricade == null) return;
        Vec3d center = Vec3d.ofCenter(barricade);
        zombie.getLookControl().lookAt(center.x, center.y, center.z);
        if (++ticks % 20 == 0) {
            zombie.swingHand(Hand.MAIN_HAND);
            if (BarricadeBlock.hit(zombie.getWorld(), barricade, zombie.barricadeDamage())) {
                barricade = null;
            }
        }
    }

    @Nullable
    private BlockPos findBarricade() {
        BlockPos origin = zombie.getBlockPos();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.iterate(origin.add(-1, 0, -1), origin.add(1, 1, 1))) {
            if (zombie.getWorld().getBlockState(pos).getBlock() instanceof BarricadeBlock) {
                double dist = pos.getSquaredDistance(zombie.getPos());
                if (dist < bestDist) {
                    bestDist = dist;
                    best = pos.toImmutable();
                }
            }
        }
        return best;
    }
}
