package com.deadhorizon.entity.zombie;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;

/** A soldier who didn't make it: wears armour and shrugs off half of all projectile damage. */
public class SoldierEntity extends DHZombieEntity {
    public SoldierEntity(EntityType<? extends ZombieEntity> type, World world) {
        super(type, world);
        this.experiencePoints = 12;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return ZombieEntity.createZombieAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.22)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.3)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        equipStack(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        equipStack(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        if (random.nextFloat() < 0.5f) equipStack(EquipmentSlot.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            setEquipmentDropChance(slot, 0.04f);
        }
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.isIn(DamageTypeTags.IS_PROJECTILE)) amount *= 0.5f;
        return super.damage(source, amount);
    }
}
