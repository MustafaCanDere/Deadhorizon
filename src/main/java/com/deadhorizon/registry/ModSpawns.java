package com.deadhorizon.registry;

import com.deadhorizon.config.DHConfig;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;

/** Adds our zombies wherever vanilla zombies already spawn. */
public final class ModSpawns {
    private ModSpawns() {
    }

    public static void register() {
        DHConfig cfg = DHConfig.INSTANCE;
        add(ModEntities.WALKER, cfg.walkerSpawnWeight, 2, 5);
        add(ModEntities.RUNNER, cfg.runnerSpawnWeight, 1, 3);
        add(ModEntities.BRUTE, cfg.bruteSpawnWeight, 1, 1);
        add(ModEntities.SCREAMER, cfg.screamerSpawnWeight, 1, 1);
        add(ModEntities.SPITTER, cfg.spitterSpawnWeight, 1, 2);
        add(ModEntities.BLOATER, cfg.bloaterSpawnWeight, 1, 1);
        add(ModEntities.CRAWLER, cfg.crawlerSpawnWeight, 1, 3);
        add(ModEntities.SOLDIER, cfg.soldierSpawnWeight, 1, 2);
    }

    private static void add(EntityType<?> type, int weight, int min, int max) {
        if (weight <= 0) return;
        BiomeModifications.addSpawn(BiomeSelectors.spawnsOneOf(EntityType.ZOMBIE), SpawnGroup.MONSTER,
                type, weight, min, max);
    }
}
