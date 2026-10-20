package com.deadhorizon.power;

import com.deadhorizon.config.DHConfig;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Wireless, short-range power. Generators and solar panels announce themselves here while they run;
 * a device is powered when a live source is within {@link DHConfig#powerRange} blocks. Sources re-announce
 * on every tick they run, so nothing needs saving: after a restart the list refills within seconds.
 */
public final class PowerGrid {
    private static final Map<RegistryKey<World>, Set<BlockPos>> SOURCES = new HashMap<>();

    private PowerGrid() {
    }

    /** Implemented by blocks that produce power; the LIT or POWERED state says whether they currently do. */
    public interface Source {
    }

    public static void announce(World world, BlockPos pos) {
        SOURCES.computeIfAbsent(world.getRegistryKey(), k -> new HashSet<>()).add(pos.toImmutable());
    }

    public static boolean isPowered(World world, BlockPos pos) {
        Set<BlockPos> sources = SOURCES.get(world.getRegistryKey());
        if (sources == null) return false;
        int range = DHConfig.INSTANCE.powerRange;
        double rangeSq = (double) range * range;
        Iterator<BlockPos> it = sources.iterator();
        while (it.hasNext()) {
            BlockPos source = it.next();
            if (!world.isChunkLoaded(source)) continue;
            if (!isLive(world.getBlockState(source))) {
                it.remove();
                continue;
            }
            if (source.getSquaredDistance(pos) <= rangeSq) return true;
        }
        return false;
    }

    private static boolean isLive(BlockState state) {
        if (!(state.getBlock() instanceof Source)) return false;
        if (state.contains(Properties.LIT)) return state.get(Properties.LIT);
        return state.contains(Properties.POWERED) && state.get(Properties.POWERED);
    }
}
