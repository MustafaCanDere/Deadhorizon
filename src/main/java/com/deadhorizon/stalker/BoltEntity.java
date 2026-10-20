package com.deadhorizon.stalker;

import com.deadhorizon.block.AnomalyBlock;
import com.deadhorizon.registry.ModEntities;
import com.deadhorizon.registry.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** A thrown bolt: sets off any anomaly it flies through, then lands where it can be picked up again. */
public class BoltEntity extends ThrownItemEntity {
    private BlockPos lastChecked;

    public BoltEntity(EntityType<? extends BoltEntity> type, World world) {
        super(type, world);
    }

    public BoltEntity(World world, LivingEntity owner) {
        super(ModEntities.BOLT, owner, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.BOLT;
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld() instanceof ServerWorld world) {
            BlockPos pos = getBlockPos();
            if (!pos.equals(lastChecked)) {
                lastChecked = pos;
                BlockState state = world.getBlockState(pos);
                if (state.getBlock() instanceof AnomalyBlock anomaly) anomaly.discharge(world, pos);
            }
        }
    }

    @Override
    protected void onCollision(HitResult hit) {
        super.onCollision(hit);
        if (!getWorld().isClient) {
            ItemEntity item = new ItemEntity(getWorld(), getX(), getY(), getZ(), new ItemStack(ModItems.BOLT));
            item.setVelocity(0, 0, 0);
            getWorld().spawnEntity(item);
            discard();
        }
    }
}
