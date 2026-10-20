package com.deadhorizon.noise;

import com.deadhorizon.config.DHConfig;
import com.deadhorizon.entity.zombie.DHZombieEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/** Noise attracts nearby Dead Horizon zombies that are not already chasing something. */
public final class NoiseSystem {
    private NoiseSystem() {
    }

    public static void emit(ServerWorld world, Vec3d pos, double radius) {
        if (!DHConfig.INSTANCE.noiseEnabled || radius <= 0) return;
        BlockPos target = BlockPos.ofFloored(pos);
        double radiusSq = radius * radius;
        Box box = Box.of(pos, radius * 2, radius * 2, radius * 2);
        for (DHZombieEntity zombie : world.getEntitiesByClass(DHZombieEntity.class, box,
                z -> z.getTarget() == null && z.squaredDistanceTo(pos) <= radiusSq)) {
            zombie.hearNoise(target);
        }
    }
}
