package com.deadhorizon.entity.zombie;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;

/** Slow, sturdy, comes in numbers. */
public class WalkerEntity extends DHZombieEntity {
    public WalkerEntity(EntityType<? extends ZombieEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return ZombieEntity.createZombieAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 24.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.5)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0);
    }
}
