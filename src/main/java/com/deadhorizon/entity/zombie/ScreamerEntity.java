package com.deadhorizon.entity.zombie;

import com.deadhorizon.noise.NoiseSystem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

/** Weak on its own, but when it spots prey it screams and every zombie nearby comes running. */
public class ScreamerEntity extends DHZombieEntity {
    private static final int COOLDOWN = 240;
    private static final double CALL_RADIUS = 24.0;
    private static final double NOISE_RADIUS = 40.0;
    private int screamCooldown = 40;

    public ScreamerEntity(EntityType<? extends ZombieEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return ZombieEntity.createZombieAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 16.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.26)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        if (screamCooldown > 0) screamCooldown--;
        LivingEntity target = getTarget();
        if (target != null && screamCooldown == 0 && canSee(target)) {
            scream(target);
            screamCooldown = COOLDOWN;
        }
    }

    private void scream(LivingEntity target) {
        if (!(getWorld() instanceof ServerWorld world)) return;
        playSound(SoundEvents.ENTITY_GHAST_SCREAM, 3.0f, 0.55f + random.nextFloat() * 0.1f);
        NoiseSystem.emit(world, getPos(), NOISE_RADIUS);
        for (DHZombieEntity zombie : world.getEntitiesByClass(DHZombieEntity.class, Box.of(getPos(), CALL_RADIUS * 2, 16, CALL_RADIUS * 2),
                z -> z != this && z.getTarget() == null)) {
            zombie.setTarget(target);
        }
    }
}
