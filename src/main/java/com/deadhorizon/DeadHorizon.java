package com.deadhorizon;

import com.deadhorizon.config.DHConfig;
import com.deadhorizon.effect.BloodEffects;
import com.deadhorizon.infection.Infection;
import com.deadhorizon.loot.LootSearch;
import com.deadhorizon.noise.NoiseEvents;
import com.deadhorizon.registry.ModBlockEntities;
import com.deadhorizon.registry.ModBlocks;
import com.deadhorizon.registry.ModEffects;
import com.deadhorizon.registry.ModEntities;
import com.deadhorizon.registry.ModItems;
import com.deadhorizon.registry.ModLoot;
import com.deadhorizon.registry.ModSpawns;
import com.deadhorizon.survival.Survival;
import com.deadhorizon.weapon.Guns;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeadHorizon implements ModInitializer {
    public static final String MOD_ID = "deadhorizon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        DHConfig.load();
        ModEffects.register();
        ModBlocks.register();
        ModBlockEntities.register();
        ModEntities.register();
        ModItems.register();
        ModSpawns.register();
        ModLoot.register();
        NoiseEvents.register();
        Infection.register();
        LootSearch.register();
        Survival.register();
        BloodEffects.register();
        Guns.register();
        LOGGER.info("Dead Horizon loaded. The dead are walking.");
    }
}
