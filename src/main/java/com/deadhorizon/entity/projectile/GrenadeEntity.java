package com.deadhorizon.entity.projectile;

import com.deadhorizon.config.DHConfig;
import com.deadhorizon.noise.NoiseSystem;
import com.deadhorizon.registry.ModEntities;
import com.deadhorizon.registry.ModItems;
import com.deadhorizon.weapon.GrenadeItem;
import com.deadhorizon.weapon.GrenadeItem.Kind;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * A thrown grenade. Frags and pipe bombs come to rest where they land and go off on a fuse (the pipe bomb
 * beeps first, drawing zombies in); a molotov bursts into flames on impact.
 */
public class GrenadeEntity extends ThrownItemEntity {
    private static final int FRAG_FUSE = 50;
    private static final int PIPE_BOMB_FUSE = 120;

    @Nullable
    private Vec3d restPos;
    private int fuse;

    public GrenadeEntity(EntityType<? extends GrenadeEntity> type, World world) {
        super(type, world);
    }

    public GrenadeEntity(World world, LivingEntity owner) {
        super(ModEntities.GRENADE, owner, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.FRAG_GRENADE;
    }

    private Kind kind() {
        return getStack().getItem() instanceof GrenadeItem grenade ? grenade.kind() : Kind.FRAG;
    }

    @Override
    public void tick() {
        if (restPos == null) {
            super.tick();
            // A frag thrown far enough can still go off in the air.
            if (!getWorld().isClient && kind() == Kind.FRAG && age >= FRAG_FUSE) explode(2.8f);
            return;
        }
        baseTick();
        setPosition(restPos);
        setVelocity(Vec3d.ZERO);
        if (getWorld().isClient) return;
        fuse++;
        if (kind() == Kind.PIPE_BOMB) {
            if (fuse % 10 == 0) {
                float pitch = 1.0f + fuse / (float) PIPE_BOMB_FUSE;
                getWorld().playSound(null, getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(),
                        SoundCategory.PLAYERS, 1.5f, pitch);
                NoiseSystem.emit((ServerWorld) getWorld(), getPos(), 40.0);
            }
            if (fuse >= PIPE_BOMB_FUSE) explode(2.5f);
        } else if (age >= FRAG_FUSE) {
            explode(2.8f);
        }
    }

    @Override
    protected void onCollision(HitResult hit) {
        if (restPos != null) return;
        if (kind() == Kind.MOLOTOV) {
            if (!getWorld().isClient) burn(hit.getPos());
            return;
        }
        if (hit instanceof BlockHitResult blockHit) {
            // Come to rest just outside the face we hit.
            Vec3d normal = Vec3d.of(blockHit.getSide().getVector());
            restPos = hit.getPos().add(normal.multiply(0.15));
            getWorld().playSound(null, getBlockPos(), SoundEvents.BLOCK_CHAIN_FALL, SoundCategory.PLAYERS, 0.8f, 1.2f);
        } else {
            setVelocity(getVelocity().multiply(-0.2)); // bounce off whoever it hit
        }
    }

    private void explode(float power) {
        World.ExplosionSourceType type = DHConfig.INSTANCE.grenadesBreakBlocks
                ? World.ExplosionSourceType.TNT : World.ExplosionSourceType.NONE;
        getWorld().createExplosion(this, getX(), getY() + 0.1, getZ(), power, type);
        NoiseSystem.emit((ServerWorld) getWorld(), getPos(), 64.0);
        discard();
    }

    private void burn(Vec3d at) {
        World world = getWorld();
        world.playSound(null, BlockPos.ofFloored(at), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1.0f, 1.0f);
        world.playSound(null, BlockPos.ofFloored(at), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 1.0f, 0.8f);
        BlockPos center = BlockPos.ofFloored(at);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx * dx + dz * dz > 5) continue;
                for (int dy = 1; dy >= -2; dy--) {
                    BlockPos pos = center.add(dx, dy, dz);
                    if (world.isAir(pos) && AbstractFireBlock.canPlaceAt(world, pos, Direction.UP)) {
                        world.setBlockState(pos, AbstractFireBlock.getState(world, pos));
                        break;
                    }
                }
            }
        }
        for (LivingEntity living : world.getEntitiesByClass(LivingEntity.class, new Box(center).expand(3.0), e -> true)) {
            living.setOnFireFor(8);
        }
        NoiseSystem.emit((ServerWorld) world, at, 24.0);
        discard();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Fuse", fuse);
        if (restPos != null) {
            nbt.putDouble("RestX", restPos.x);
            nbt.putDouble("RestY", restPos.y);
            nbt.putDouble("RestZ", restPos.z);
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        fuse = nbt.getInt("Fuse");
        if (nbt.contains("RestX")) restPos = new Vec3d(nbt.getDouble("RestX"), nbt.getDouble("RestY"), nbt.getDouble("RestZ"));
    }
}
