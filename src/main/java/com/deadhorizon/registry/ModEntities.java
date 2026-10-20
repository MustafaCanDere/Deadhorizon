package com.deadhorizon.registry;

import com.deadhorizon.DeadHorizon;
import com.deadhorizon.config.DHConfig;
import com.deadhorizon.entity.projectile.AcidSpitEntity;
import com.deadhorizon.entity.zombie.BloaterEntity;
import com.deadhorizon.entity.zombie.BruteEntity;
import com.deadhorizon.entity.zombie.CrawlerEntity;
import com.deadhorizon.entity.zombie.ScreamerEntity;
import com.deadhorizon.entity.zombie.SoldierEntity;
import com.deadhorizon.entity.zombie.SpitterEntity;
import com.deadhorizon.entity.zombie.DHZombieEntity;
import com.deadhorizon.entity.zombie.RunnerEntity;
import com.deadhorizon.entity.zombie.WalkerEntity;
import com.deadhorizon.entity.misc.SeatEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ServerWorldAccess;

import java.util.function.Supplier;

public final class ModEntities {
    public static final EntityType<WalkerEntity> WALKER =
            register("walker", WalkerEntity::new, 0.6f, 1.95f, WalkerEntity::createAttributes);
    public static final EntityType<RunnerEntity> RUNNER =
            register("runner", RunnerEntity::new, 0.6f, 1.95f, RunnerEntity::createAttributes);
    public static final EntityType<BruteEntity> BRUTE =
            register("brute", BruteEntity::new, 0.9f, 2.6f, BruteEntity::createAttributes);
    public static final EntityType<ScreamerEntity> SCREAMER =
            register("screamer", ScreamerEntity::new, 0.6f, 1.95f, ScreamerEntity::createAttributes);
    public static final EntityType<SpitterEntity> SPITTER =
            register("spitter", SpitterEntity::new, 0.6f, 1.95f, SpitterEntity::createAttributes);
    public static final EntityType<BloaterEntity> BLOATER =
            register("bloater", BloaterEntity::new, 0.85f, 2.1f, BloaterEntity::createAttributes);
    public static final EntityType<CrawlerEntity> CRAWLER =
            register("crawler", CrawlerEntity::new, 0.8f, 0.6f, CrawlerEntity::createAttributes);
    public static final EntityType<SoldierEntity> SOLDIER =
            register("soldier", SoldierEntity::new, 0.6f, 1.95f, SoldierEntity::createAttributes);

    public static final EntityType<AcidSpitEntity> ACID_SPIT = Registry.register(Registries.ENTITY_TYPE, DeadHorizon.id("acid_spit"),
            FabricEntityTypeBuilder.<AcidSpitEntity>create(SpawnGroup.MISC, AcidSpitEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackRangeChunks(4)
                    .trackedUpdateRate(10)
                    .build());

    public static final EntityType<SeatEntity> SEAT = Registry.register(Registries.ENTITY_TYPE, DeadHorizon.id("seat"),
            FabricEntityTypeBuilder.<SeatEntity>create(SpawnGroup.MISC, SeatEntity::new)
                    .dimensions(EntityDimensions.fixed(0.001f, 0.001f))
                    .disableSaving()
                    .disableSummon()
                    .fireImmune()
                    .build());

    private ModEntities() {
    }

    private static <T extends DHZombieEntity> EntityType<T> register(String name, EntityType.EntityFactory<T> factory,
                                                                      float width, float height,
                                                                      Supplier<DefaultAttributeContainer.Builder> attributes) {
        EntityType<T> type = FabricEntityTypeBuilder.<T>createMob()
                .spawnGroup(SpawnGroup.MONSTER)
                .entityFactory(factory)
                .dimensions(EntityDimensions.fixed(width, height))
                .trackRangeChunks(8)
                .defaultAttributes(attributes)
                .spawnRestriction(SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                        ModEntities::canSpawn)
                .build();
        return Registry.register(Registries.ENTITY_TYPE, DeadHorizon.id(name), type);
    }

    private static <T extends DHZombieEntity> boolean canSpawn(EntityType<T> type, ServerWorldAccess world,
                                                               SpawnReason reason, BlockPos pos, Random random) {
        return DHConfig.INSTANCE.daylightSpawning
                ? HostileEntity.canSpawnIgnoreLightLevel(type, world, reason, pos, random)
                : HostileEntity.canSpawnInDark(type, world, reason, pos, random);
    }

    public static void register() {
        // Static initialisers above do the work; this forces class loading at init time.
    }
}
