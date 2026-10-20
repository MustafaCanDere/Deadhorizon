package com.deadhorizon.entity.zombie;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;

/** Slow and swollen; bursts into a toxic cloud when killed, so kill it from a distance. */
public class BloaterEntity extends DHZombieEntity {
    public BloaterEntity(EntityType<? extends ZombieEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 10;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return ZombieEntity.createZombieAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.17)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.5)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (getWorld().isClient) return;
        getWorld().createExplosion(this, getX(), getBodyY(0.5), getZ(), 1.5f, World.ExplosionSourceType.NONE);
        AreaEffectCloudEntity cloud = new AreaEffectCloudEntity(getWorld(), getX(), getY(), getZ());
        cloud.setRadius(3.5f);
        cloud.setRadiusOnUse(-0.3f);
        cloud.setWaitTime(0);
        cloud.setDuration(200);
        cloud.setRadiusGrowth(-cloud.getRadius() / cloud.getDuration());
        cloud.addEffect(new StatusEffectInstance(StatusEffects.POISON, 120, 1));
        cloud.addEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 160, 0));
        getWorld().spawnEntity(cloud);
    }
}
