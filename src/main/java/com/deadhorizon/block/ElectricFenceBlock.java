package com.deadhorizon.block;

import com.deadhorizon.config.DHConfig;
import com.deadhorizon.registry.ModDamageTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Map;

import static com.deadhorizon.block.Shapes.box;

/** A wire fence panel. Powered, it shocks and throws back whatever touches it; unpowered it is just a fence. */
public class ElectricFenceBlock extends ElectricDeviceBlock {
    /** Taller than it looks, like a vanilla fence, so nothing jumps over. */
    private static final Map<Direction, VoxelShape> COLLISION = Shapes.horizontal(box(0, 0, 7, 16, 24, 9));

    public ElectricFenceBlock(Settings settings) {
        super(settings, box(0, 0, 7, 16, 16, 9));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return COLLISION.get(state.get(FACING));
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.isClient || !state.get(POWERED) || !(entity instanceof LivingEntity living)) return;
        if (living instanceof PlayerEntity player && (player.isCreative() || player.isSpectator())) return;
        float damage = DHConfig.INSTANCE.electricFenceDamage * (living instanceof PlayerEntity ? 0.5f : 1.0f);
        if (living.damage(ModDamageTypes.of(world, ModDamageTypes.ELECTROCUTION), damage)) {
            living.takeKnockback(0.6, pos.getX() + 0.5 - living.getX(), pos.getZ() + 0.5 - living.getZ());
            world.playSound(null, pos, SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.BLOCKS, 0.3f, 2.0f);
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(POWERED) && random.nextInt(3) == 0) {
            world.addParticle(ParticleTypes.ELECTRIC_SPARK, pos.getX() + random.nextDouble(),
                    pos.getY() + 0.3 + random.nextDouble() * 0.6, pos.getZ() + random.nextDouble(), 0, 0, 0);
        }
    }
}
