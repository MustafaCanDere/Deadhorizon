package com.deadhorizon.entity.zombie;

import com.deadhorizon.config.DHConfig;
import com.deadhorizon.entity.ai.InvestigateNoiseGoal;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.ai.goal.BreakDoorGoal;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

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

    @Override
    protected void initCustomGoals() {
        super.initCustomGoals();
        this.goalSelector.add(1, new BreakDoorGoal(this, doorBreakTime(),
                difficulty -> DHConfig.INSTANCE.doorBreakingOnAllDifficulties || difficulty == Difficulty.HARD));
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
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
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
