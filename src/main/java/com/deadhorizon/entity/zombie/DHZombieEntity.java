package com.deadhorizon.entity.zombie;

import com.deadhorizon.config.DHConfig;
import com.deadhorizon.entity.ai.BreakBarricadeGoal;
import com.deadhorizon.entity.ai.InvestigateNoiseGoal;
import com.deadhorizon.registry.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.ai.goal.BreakDoorGoal;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Shared base for all Dead Horizon zombies: no daylight burning, no drowning conversion,
 * door breaking on every difficulty (configurable) and reacting to noise.
 */
public abstract class DHZombieEntity extends ZombieEntity {
    @Nullable
    private BlockPos noisePos;

    protected DHZombieEntity(EntityType<? extends ZombieEntity> type, World world) {
        super(type, world);
    }

    /** Ticks needed to break a door; vanilla is 240. */
    protected int doorBreakTime() {
        return 240;
    }

    /** How much damage each blow does to a barricade (it breaks after more than 3). */
    public int barricadeDamage() {
        return 1;
    }

    @Override
    protected void initCustomGoals() {
        super.initCustomGoals();
        this.goalSelector.add(1, new BreakDoorGoal(this, doorBreakTime(),
                difficulty -> DHConfig.INSTANCE.doorBreakingOnAllDifficulties || difficulty == Difficulty.HARD));
        this.goalSelector.add(1, new BreakBarricadeGoal(this));
        this.goalSelector.add(6, new InvestigateNoiseGoal(this, 1.0));
        if (NavigationConditions.hasMobNavigation(this)) {
            ((MobNavigation) this.getNavigation()).setCanPathThroughDoors(true);
        }
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason,
                                 @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        // No babies or chicken jockeys for our variants.
        if (entityData == null) {
            entityData = new ZombieEntity.ZombieData(false, false);
        }
        EntityData data = super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
        if (DHConfig.INSTANCE.dayScaling) scaleWithDay(world.toServerWorld().getTimeOfDay() / 24000L);
        return data;
    }

    /** The longer the world has gone on, the tougher new zombies are: +3% health a day (max +100%) and so on. */
    private void scaleWithDay(long days) {
        if (days <= 0) return;
        addModifier(EntityAttributes.GENERIC_MAX_HEALTH, "Day scaling health", Math.min(1.0, days * 0.03));
        addModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, "Day scaling damage", Math.min(0.5, days * 0.015));
        addModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, "Day scaling speed", Math.min(0.15, days * 0.005));
        setHealth(getMaxHealth());
    }

    private void addModifier(EntityAttribute attribute, String name, double amount) {
        EntityAttributeInstance instance = getAttributeInstance(attribute);
        if (instance != null) {
            instance.addPersistentModifier(new EntityAttributeModifier(name, amount, EntityAttributeModifier.Operation.MULTIPLY_BASE));
        }
    }

    /** Projectiles that hit the head do extra damage. */
    @Override
    public boolean damage(DamageSource source, float amount) {
        if (DHConfig.INSTANCE.headshots && source.isIn(DamageTypeTags.IS_PROJECTILE) && isHeadshot(source.getSource())) {
            amount *= (float) DHConfig.INSTANCE.headshotMultiplier;
            this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_ARROW_HIT_PLAYER,
                    SoundCategory.PLAYERS, 0.6f, 0.5f);
        }
        if (source.getAttacker() instanceof PlayerEntity player && source.getSource() == player
                && !source.isIn(DamageTypeTags.IS_PROJECTILE)) {
            amount = melee(player, amount);
        }
        return super.damage(source, amount);
    }

    /** Blades take heads off on a critical hit; blunt weapons stagger. */
    private float melee(PlayerEntity player, float amount) {
        ItemStack weapon = player.getMainHandStack();
        boolean critical = player.fallDistance > 0.0f && !player.isOnGround() && !player.isClimbing() && !player.isTouchingWater();
        if (DHConfig.INSTANCE.decapitation && critical
                && (weapon.isOf(ModItems.KATANA) || weapon.isOf(ModItems.MACHETE) || weapon.isOf(ModItems.FIRE_AXE))) {
            this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1.0f, 0.6f);
            return Math.max(amount, this.getHealth() + 20.0f);
        }
        if (weapon.isOf(ModItems.BASEBALL_BAT) || weapon.isOf(ModItems.NAIL_BAT) || weapon.isOf(ModItems.CROWBAR)) {
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 30, 3));
            this.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 30, 1));
        }
        return amount;
    }

    private boolean isHeadshot(@Nullable Entity projectile) {
        // Hitscan guns work out their own headshots; this is for arrows and other real projectiles.
        if (!(projectile instanceof ProjectileEntity)) return false;
        // The projectile is still where it was at the start of the tick, so trace its path into our box.
        Vec3d start = projectile.getPos();
        Vec3d end = start.add(projectile.getVelocity());
        Optional<Vec3d> hit = this.getBoundingBox().expand(0.3).raycast(start, end);
        double y = hit.map(v -> v.y).orElse(projectile.getY());
        double headBottom = this.getY() + this.getHeight() * 0.75;
        return y >= headBottom;
    }

    @Override
    protected boolean burnsInDaylight() {
        return false;
    }

    @Override
    protected boolean canConvertInWater() {
        return false;
    }

    /** Lets the natural spawner place us in bright light; the spawn predicate decides the rest. */
    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        return DHConfig.INSTANCE.daylightSpawning ? 0.0f : super.getPathfindingFavor(pos, world);
    }

    public void hearNoise(BlockPos pos) {
        this.noisePos = pos;
    }

    @Nullable
    public BlockPos getNoisePos() {
        return noisePos;
    }

    public void clearNoise() {
        this.noisePos = null;
    }
}
