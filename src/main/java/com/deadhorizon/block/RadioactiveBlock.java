package com.deadhorizon.block;

import net.minecraft.block.BlockState;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

/** Contaminated junk. Gives off {@link #strength()} rad/s up close, falling off over about six blocks. */
public class RadioactiveBlock extends FurnitureBlock {
    private static final DustParticleEffect GLOW = new DustParticleEffect(new Vector3f(0.5f, 1.0f, 0.2f), 0.6f);
    private final float strength;

    public RadioactiveBlock(Settings settings, float strength, double[]... boxes) {
        super(settings, boxes);
        this.strength = strength;
    }

    public float strength() {
        return strength;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(3) != 0) return;
        Vec3d p = Vec3d.ofCenter(pos).add(random.nextDouble() - 0.5, random.nextDouble() * 0.6, random.nextDouble() - 0.5);
        world.addParticle(GLOW, p.x, p.y, p.z, 0, 0.01, 0);
    }
}
