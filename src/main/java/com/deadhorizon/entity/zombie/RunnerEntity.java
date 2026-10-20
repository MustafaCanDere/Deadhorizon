package com.deadhorizon.entity.zombie;

import com.deadhorizon.config.DHConfig;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;

import java.util.UUID;

/** Fast and fragile; even faster at night. */
public class RunnerEntity extends DHZombieEntity {
    private static final UUID NIGHT_SPEED_ID = UUID.fromString("5f2b8f0e-3c1a-4d0b-9a59-7a3c2f6c1e01");
    private static final EntityAttributeModifier NIGHT_SPEED = new EntityAttributeModifier(
            NIGHT_SPEED_ID, "Runner night frenzy", 0.25, EntityAttributeModifier.Operation.MULTIPLY_BASE);

    public RunnerEntity(EntityType<? extends ZombieEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return ZombieEntity.createZombieAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 14.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.32)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        if (this.age % 20 != 0) return;
        EntityAttributeInstance speed = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speed == null) return;
        boolean frenzy = DHConfig.INSTANCE.runnersFasterAtNight && this.getWorld().isNight();
        if (frenzy && !speed.hasModifier(NIGHT_SPEED)) {
            speed.addTemporaryModifier(NIGHT_SPEED);
        } else if (!frenzy && speed.hasModifier(NIGHT_SPEED)) {
            speed.removeModifier(NIGHT_SPEED_ID);
        }
    }
}
