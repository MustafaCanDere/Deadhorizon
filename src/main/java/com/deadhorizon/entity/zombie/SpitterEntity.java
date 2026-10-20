package com.deadhorizon.entity.zombie;

import com.deadhorizon.entity.projectile.AcidSpitEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/** Keeps its distance and spits acid that poisons and sickens. */
public class SpitterEntity extends DHZombieEntity implements RangedAttackMob {
    public SpitterEntity(EntityType<? extends ZombieEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return ZombieEntity.createZombieAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 18.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void initCustomGoals() {
        super.initCustomGoals();
        // Outranks the inherited melee goal, so it spits whenever it has a target.
        this.goalSelector.add(1, new ProjectileAttackGoal(this, 1.0, 40, 14.0f));
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        AcidSpitEntity spit = new AcidSpitEntity(getWorld(), this);
        double dx = target.getX() - getX();
        double dy = target.getBodyY(0.3333) - spit.getY();
        double dz = target.getZ() - getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        spit.setVelocity(dx, dy + horizontal * 0.2, dz, 1.2f, 6.0f);
        playSound(SoundEvents.ENTITY_LLAMA_SPIT, 1.0f, 0.6f);
        getWorld().spawnEntity(spit);
    }
}
