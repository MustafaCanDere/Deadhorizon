package com.deadhorizon.entity.npc;

import com.deadhorizon.entity.zombie.DHZombieEntity;
import com.deadhorizon.registry.ModItems;
import com.deadhorizon.weapon.GunItem;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LongDoorInteractGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * An armed human. Bandits shoot players and survivors on sight; survivors (stalkers) hunt zombies and bandits
 * and only turn on a player who attacks them. Both fight with whatever gun they carry, reload, and run when hurt.
 */
public class HumanNpcEntity extends PathAwareEntity {
    public enum Faction { BANDIT, SURVIVOR }

    private final Faction faction;
    private boolean armed;
    int rounds;

    public HumanNpcEntity(EntityType<? extends PathAwareEntity> type, World world, Faction faction) {
        super(type, world);
        this.faction = faction;
        this.experiencePoints = 10;
        if (getNavigation() instanceof MobNavigation nav) nav.setCanPathThroughDoors(true);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 24.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0)
                .add(EntityAttributes.GENERIC_ARMOR, 2.0);
    }

    public Faction faction() {
        return faction;
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new NpcFleeGoal(this));
        goalSelector.add(2, new NpcShootGoal(this));
        goalSelector.add(3, new MeleeAttackGoal(this, 1.15, false));
        goalSelector.add(4, new LongDoorInteractGoal(this, true));
        goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 10.0f));
        goalSelector.add(7, new LookAroundGoal(this));

        targetSelector.add(1, new RevengeGoal(this).setGroupRevenge());
        if (faction == Faction.BANDIT) {
            targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
            targetSelector.add(3, new ActiveTargetGoal<>(this, HumanNpcEntity.class, 10, true, false,
                    e -> e instanceof HumanNpcEntity h && h.faction != Faction.BANDIT));
            targetSelector.add(4, new ActiveTargetGoal<>(this, DHZombieEntity.class, true));
        } else {
            targetSelector.add(2, new ActiveTargetGoal<>(this, DHZombieEntity.class, true));
            targetSelector.add(3, new ActiveTargetGoal<>(this, HumanNpcEntity.class, 10, true, false,
                    e -> e instanceof HumanNpcEntity h && h.faction == Faction.BANDIT));
        }
    }

    @Nullable
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason reason,
                                 @Nullable EntityData data, @Nullable NbtCompound nbt) {
        EntityData result = super.initialize(world, difficulty, reason, data, nbt);
        arm(getRandom());
        return result;
    }

    /** Also called on the first tick of NPCs placed by structures, which skip initialize(). */
    private void arm(Random random) {
        armed = true;
        float roll = random.nextFloat();
        Item weapon;
        if (faction == Faction.BANDIT) {
            weapon = roll < 0.4f ? ModItems.PISTOL : roll < 0.6f ? ModItems.DOUBLE_BARREL : roll < 0.75f ? ModItems.AK47
                    : roll < 0.85f ? ModItems.UZI : roll < 0.93f ? ModItems.MACHETE : ModItems.BASEBALL_BAT;
            equipStack(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
            if (random.nextBoolean()) equipStack(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
        } else {
            weapon = roll < 0.3f ? ModItems.PISTOL : roll < 0.5f ? ModItems.SHOTGUN : roll < 0.7f ? ModItems.HUNTING_RIFLE
                    : roll < 0.85f ? ModItems.AK47 : roll < 0.95f ? ModItems.M4 : ModItems.MACHETE;
            equipStack(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
            equipStack(EquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
        }
        ItemStack stack = new ItemStack(weapon);
        if (weapon instanceof GunItem gun) {
            rounds = gun.magazine(stack);
            // What drops when they die: a worn gun with whatever was left in it.
            stack.setDamage((int) (stack.getMaxDamage() * (0.3f + random.nextFloat() * 0.5f)));
            GunItem.setAmmo(stack, random.nextInt(Math.max(1, rounds / 3)));
        }
        equipStack(EquipmentSlot.MAINHAND, stack);
        setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.35f);
        setEquipmentDropChance(EquipmentSlot.CHEST, 0.08f);
        setEquipmentDropChance(EquipmentSlot.HEAD, 0.08f);
        setEquipmentDropChance(EquipmentSlot.LEGS, 0.08f);
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) return;
        if (!armed) arm(getRandom());
        if (age % 60 == 0 && getTarget() == null && getHealth() < getMaxHealth()) heal(1.0f);
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return !isPersistent();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return faction == Faction.BANDIT ? SoundEvents.ENTITY_PILLAGER_AMBIENT : SoundEvents.ENTITY_VILLAGER_AMBIENT;
    }

    @Override
    public int getMinAmbientSoundDelay() {
        return 240;
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.entity.damage.DamageSource source) {
        return SoundEvents.ENTITY_PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PLAYER_DEATH;
    }

    @Override
    public float getSoundPitch() {
        return faction == Faction.BANDIT ? 0.75f : 0.9f;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("DHArmed", armed);
        nbt.putInt("Rounds", rounds);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        armed = nbt.getBoolean("DHArmed");
        rounds = nbt.getInt("Rounds");
    }
}
