package com.deadhorizon.config;

import com.deadhorizon.DeadHorizon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** Simple JSON config at config/deadhorizon.json. Missing fields keep their defaults. */
public class DHConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("deadhorizon.json");

    public static DHConfig INSTANCE = new DHConfig();

    // Spawning
    public boolean daylightSpawning = true;
    public int walkerSpawnWeight = 100;
    public int runnerSpawnWeight = 40;
    public int bruteSpawnWeight = 10;
    public int screamerSpawnWeight = 8;
    public int spitterSpawnWeight = 10;
    public int bloaterSpawnWeight = 8;
    public int crawlerSpawnWeight = 15;
    public int soldierSpawnWeight = 6;

    // Behaviour
    public boolean doorBreakingOnAllDifficulties = true;
    public boolean runnersFasterAtNight = true;

    // Noise
    public boolean noiseEnabled = true;
    public double sprintNoiseRadius = 12.0;
    public double blockBreakNoiseRadius = 10.0;
    public double attackNoiseRadius = 8.0;
    public double doorNoiseRadius = 6.0;
    public double landingNoiseRadius = 8.0;

    // Infection
    public boolean infectionEnabled = true;
    public boolean vanillaZombiesInfect = true;
    public double infectionChance = 0.15;
    public int infectionMinutes = 30;
    public int antibioticsDelayMinutes = 10;
    public int antidoteImmunityMinutes = 5;
    public boolean infectedPlayersTurn = true;

    // Bleeding
    public boolean bleedingEnabled = true;
    public double bleedChance = 0.2;
    public int bleedSeconds = 60;

    // Thirst (0-20, like hunger). Default drain empties a full bar in about half an hour of play.
    public boolean thirstEnabled = true;
    public double thirstDrainPerTick = 0.0006;
    public double dirtyWaterSicknessChance = 0.35;

    // Stamina (0-100)
    public boolean staminaEnabled = true;
    public double sprintStaminaCost = 0.5;
    public double attackStaminaCost = 4.0;
    public double staminaRegenPerTick = 0.6;

    // Body temperature, sleep, carrying weight
    public boolean temperatureEnabled = true;
    public boolean fatigueEnabled = true;
    /** Rest lost per tick awake; the default lasts about two and a half in-game days. */
    public double fatigueDrainPerTick = 0.0017;
    public boolean encumbranceEnabled = true;

    /** Soaked clothes make you colder; water and rain soak you, fire and sun dry you. */
    public boolean wetnessEnabled = true;

    // Food
    public boolean foodSpoilageEnabled = true;
    /** 1.0 = raw meat lasts two in-game days; 2.0 doubles every shelf life. */
    public double foodShelfLifeMultiplier = 1.0;
    public boolean rawFoodPoisoning = true;

    // Injuries
    public boolean fracturesEnabled = true;
    public int fractureMinutes = 10;
    /** Chance that one of our zombies grabs you on a hit, slowing you right down for a moment. */
    public double zombieGrabChance = 0.35;
    /** At or below this much health you go into shock: slow and weak. 0 turns it off. */
    public double shockHealth = 4.0;
    /** Chance that a Brute's hit breaks your arm. */
    public double bruteBoneBreakChance = 0.2;
    /** Chance, every 30 seconds spent cold or soaked, of catching a cold. */
    public boolean illnessEnabled = true;
    public double coldCatchChance = 0.06;
    public int coldMinutes = 10;
    public double coughNoiseRadius = 12.0;
    public boolean bloodEffects = true;

    // World events
    /** Zombies get tougher every day survived (health, damage, speed), up to a cap. */
    public boolean dayScaling = true;
    /** A Blood Moon every this many nights; 0 turns it off. */
    public int bloodMoonEveryDays = 7;
    public boolean hordesEnabled = true;
    public int hordeMinMinutes = 20;
    public int hordeMaxMinutes = 40;
    /** A supply drop every this many days; 0 turns it off. */
    public int supplyDropEveryDays = 2;

    // Firearms
    /** Chance per shot of a jam once a gun is below 30% condition. */
    public double gunJamChance = 0.05;
    public boolean bulletsBreakGlass = true;
    public boolean grenadesBreakBlocks = false;
    /** Critical hits with a katana, machete or fire axe take a zombie's head off. */
    public boolean decapitation = true;

    // Zombie senses and headshots
    public boolean zombieSensesEnabled = true;
    public double darknessVisibility = 0.55;
    public double bleedingScentMultiplier = 1.5;
    public boolean headshots = true;
    public double headshotMultiplier = 2.0;

    public static void load() {
        try {
            if (Files.exists(PATH)) {
                try (Reader reader = Files.newBufferedReader(PATH)) {
                    DHConfig loaded = GSON.fromJson(reader, DHConfig.class);
                    if (loaded != null) INSTANCE = loaded;
                }
            }
            save();
        } catch (Exception e) {
            DeadHorizon.LOGGER.error("Could not read {}, using defaults", PATH, e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException e) {
            DeadHorizon.LOGGER.error("Could not write {}", PATH, e);
        }
    }
}
