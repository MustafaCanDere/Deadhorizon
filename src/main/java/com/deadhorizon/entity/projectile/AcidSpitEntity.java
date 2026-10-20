package com.deadhorizon.entity.projectile;

import com.deadhorizon.registry.ModEntities;
import com.deadhorizon.registry.ModItems;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

/** The Spitter's acid: hurts and poisons on a direct hit, leaves a small puddle of fumes otherwise. */
public class AcidSpitEntity extends ThrownItemEntity {
    public AcidSpitEntity(EntityType<? extends AcidSpitEntity> type, World world) {
        super(type, world);
    }

    public AcidSpitEntity(World world, LivingEntity owner) {
        super(ModEntities.ACID_SPIT, owner, world);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.ACID_GLOB;
    }

    @Override
    protected void onEntityHit(EntityHitResult hit) {
        super.onEntityHit(hit);
        Entity target = hit.getEntity();
        LivingEntity owner = getOwner() instanceof LivingEntity living ? living : null;
        target.damage(getDamageSources().mobProjectile(this, owner), 3.0f);
        if (target instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 0), owner);
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 120, 0), owner);
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult hit) {
        super.onBlockHit(hit);
        if (getWorld().isClient) return;
        AreaEffectCloudEntity cloud = new AreaEffectCloudEntity(getWorld(), getX(), getY(), getZ());
        if (getOwner() instanceof LivingEntity owner) cloud.setOwner(owner);
        cloud.setRadius(1.5f);
        cloud.setWaitTime(0);
        cloud.setDuration(80);
        cloud.addEffect(new StatusEffectInstance(StatusEffects.POISON, 60, 0));
        getWorld().spawnEntity(cloud);
    }

    @Override
    protected void onCollision(HitResult hit) {
        super.onCollision(hit);
        if (!getWorld().isClient) discard();
    }
}
