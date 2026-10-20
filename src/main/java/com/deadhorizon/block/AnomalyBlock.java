package com.deadhorizon.block;

import com.deadhorizon.config.DHConfig;
import com.deadhorizon.registry.ModDamageTypes;
import com.deadhorizon.registry.ModItems;
import com.deadhorizon.stalker.ArtifactItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * An invisible trap of the Zone. Only a faint shimmer gives it away; a thrown bolt sets it off harmlessly and a
 * detector in hand shows its outline. Now and then it grows an artifact nearby.
 */
public class AnomalyBlock extends Block {
    public enum Kind { BURNER, ELECTRO, SPRINGBOARD }

    private final Kind kind;

    public AnomalyBlock(Settings settings, Kind kind) {
        super(settings.ticksRandomly());
        this.kind = kind;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (context instanceof EntityShapeContext ctx && ctx.getEntity() instanceof PlayerEntity player
                && (player.isCreative() || player.isHolding(ModItems.DETECTOR))) {
            return VoxelShapes.fullCube();
        }
        return VoxelShapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.isClient || !DHConfig.INSTANCE.anomaliesEnabled || !(entity instanceof LivingEntity living)) return;
        if (living instanceof PlayerEntity p && (p.isCreative() || p.isSpectator())) return;
        if (living.timeUntilRegen > 10) return;
        boolean hit = switch (kind) {
            case BURNER -> {
                living.setOnFireFor(6);
                yield living.damage(world.getDamageSources().inFire(), 6.0f);
            }
            case ELECTRO -> living.damage(ModDamageTypes.of(world, ModDamageTypes.ELECTROCUTION), 7.0f);
            case SPRINGBOARD -> {
                Vec3d push = living.getPos().subtract(Vec3d.ofCenter(pos)).multiply(1, 0, 1).normalize();
                living.addVelocity(push.x * 0.8, 1.3, push.z * 0.8);
                living.velocityModified = true;
                yield living.damage(world.getDamageSources().magic(), 4.0f);
            }
        };
        if (hit) discharge((ServerWorld) world, pos);
    }

    /** The visible and audible burst when something sets it off (also used by thrown bolts). */
    public void discharge(ServerWorld world, BlockPos pos) {
        Vec3d c = Vec3d.ofCenter(pos);
        switch (kind) {
            case BURNER -> {
                world.spawnParticles(ParticleTypes.FLAME, c.x, c.y, c.z, 40, 0.3, 0.8, 0.3, 0.05);
                world.playSound(null, pos, SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.BLOCKS, 1.0f, 0.6f);
            }
            case ELECTRO -> {
                world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, c.x, c.y, c.z, 50, 0.8, 0.8, 0.8, 0.3);
                world.playSound(null, pos, SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.BLOCKS, 0.8f, 1.5f);
            }
            case SPRINGBOARD -> {
                world.spawnParticles(ParticleTypes.EXPLOSION, c.x, c.y, c.z, 1, 0, 0, 0, 0);
                world.spawnParticles(ParticleTypes.CLOUD, c.x, c.y, c.z, 20, 0.6, 0.2, 0.6, 0.05);
                world.playSound(null, pos, SoundEvents.ENTITY_WARDEN_SONIC_BOOM, SoundCategory.BLOCKS, 0.3f, 1.8f);
            }
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        double x = pos.getX() + random.nextDouble(), y = pos.getY() + random.nextDouble(), z = pos.getZ() + random.nextDouble();
        switch (kind) {
            case BURNER -> {
                if (random.nextInt(4) == 0) world.addParticle(ParticleTypes.SMALL_FLAME, x, pos.getY() + 0.05, z, 0, 0.02, 0);
                world.addParticle(ParticleTypes.WHITE_ASH, x, y, z, 0, 0.01, 0);
            }
            case ELECTRO -> {
                if (random.nextInt(3) == 0) world.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, 0, 0, 0);
            }
            case SPRINGBOARD -> {
                if (random.nextInt(2) == 0) world.addParticle(ParticleTypes.REVERSE_PORTAL, x, pos.getY() + 0.1, z, 0, 0.01, 0);
            }
        }
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!DHConfig.INSTANCE.anomaliesEnabled || random.nextFloat() >= DHConfig.INSTANCE.artifactSpawnChance) return;
        if (!world.getEntitiesByClass(ItemEntity.class, new Box(pos).expand(8),
                e -> e.getStack().getItem() instanceof ArtifactItem).isEmpty()) return;
        BlockPos spot = pos.add(random.nextInt(5) - 2, 0, random.nextInt(5) - 2);
        if (!world.getBlockState(spot).isAir() && !(world.getBlockState(spot).getBlock() instanceof AnomalyBlock)) return;
        ItemEntity artifact = new ItemEntity(world, spot.getX() + 0.5, spot.getY() + 0.2, spot.getZ() + 0.5,
                new ItemStack(artifact()), 0, 0, 0);
        artifact.setNeverDespawn();
        world.spawnEntity(artifact);
    }

    private Item artifact() {
        return switch (kind) {
            case BURNER -> ModItems.ARTIFACT_FIREBALL;
            case ELECTRO -> ModItems.ARTIFACT_STONE_FLOWER;
            case SPRINGBOARD -> ModItems.ARTIFACT_JELLYFISH;
        };
    }
}
