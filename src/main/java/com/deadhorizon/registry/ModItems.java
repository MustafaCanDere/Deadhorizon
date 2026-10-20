package com.deadhorizon.registry;

import com.deadhorizon.DeadHorizon;
import com.deadhorizon.item.DrinkItem;
import com.deadhorizon.item.MedicalItem;
import com.deadhorizon.item.MedicalItem.Treatment;
import com.deadhorizon.item.TooltipItem;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public final class ModItems {
    public static final Item WALKER_SPAWN_EGG = register("walker_spawn_egg",
            new SpawnEggItem(ModEntities.WALKER, 0x4F6B45, 0x3C4F6E, new Item.Settings()));
    public static final Item RUNNER_SPAWN_EGG = register("runner_spawn_egg",
            new SpawnEggItem(ModEntities.RUNNER, 0x96A58C, 0x822828, new Item.Settings()));
    public static final Item BRUTE_SPAWN_EGG = register("brute_spawn_egg",
            new SpawnEggItem(ModEntities.BRUTE, 0x465F3C, 0x3C462D, new Item.Settings()));
    public static final Item SCREAMER_SPAWN_EGG = register("screamer_spawn_egg",
            new SpawnEggItem(ModEntities.SCREAMER, 0xB8B0A0, 0x7A1010, new Item.Settings()));
    public static final Item SPITTER_SPAWN_EGG = register("spitter_spawn_egg",
            new SpawnEggItem(ModEntities.SPITTER, 0x8FA43A, 0xC8D640, new Item.Settings()));
    public static final Item BLOATER_SPAWN_EGG = register("bloater_spawn_egg",
            new SpawnEggItem(ModEntities.BLOATER, 0x6E5A6E, 0x9AB05A, new Item.Settings()));
    public static final Item CRAWLER_SPAWN_EGG = register("crawler_spawn_egg",
            new SpawnEggItem(ModEntities.CRAWLER, 0x4A5040, 0x3A2A20, new Item.Settings()));
    public static final Item SOLDIER_SPAWN_EGG = register("soldier_spawn_egg",
            new SpawnEggItem(ModEntities.SOLDIER, 0x55603A, 0x3A4228, new Item.Settings()));
    public static final Item ACID_GLOB = register("acid_glob", new Item(new Item.Settings()));

    // Treatment(useTicks, heal, stopsBleeding, slowsInfection, curesInfection, curesPoison, regenSeconds, curesFracture)
    public static final Item BANDAGE = register("bandage", new MedicalItem(new Item.Settings().maxCount(16),
            new Treatment(30, 2.0f, true, false, false, false, 0, false)));
    public static final Item FIRST_AID_KIT = register("first_aid_kit", new MedicalItem(new Item.Settings().maxCount(4),
            new Treatment(60, 10.0f, true, false, false, true, 0, false)));
    public static final Item PAINKILLERS = register("painkillers", new MedicalItem(new Item.Settings().maxCount(16),
            new Treatment(20, 0.0f, false, false, false, false, 10, false)));
    public static final Item ANTIBIOTICS = register("antibiotics", new MedicalItem(new Item.Settings().maxCount(16),
            new Treatment(20, 0.0f, false, true, false, false, 0, false)));
    public static final Item ANTIDOTE = register("antidote", new MedicalItem(new Item.Settings().maxCount(4),
            new Treatment(40, 4.0f, false, false, true, true, 0, false)));
    public static final Item SPLINT = register("splint", new MedicalItem(new Item.Settings().maxCount(8),
            new Treatment(80, 0.0f, false, false, false, false, 0, true)));

    // Drinks: DrinkItem(settings, thirst, stamina, rest, warmth, returnsBottle, effect, effectSeconds)
    public static final Item CLEAN_WATER = register("clean_water", new DrinkItem(new Item.Settings().maxCount(16),
            8.0f, 0.0f, 0.0f, 0.0f, true, null, 0));
    public static final Item SODA = register("soda", new DrinkItem(new Item.Settings().maxCount(16),
            6.0f, 15.0f, 5.0f, 0.0f, false, null, 0));
    public static final Item ENERGY_DRINK = register("energy_drink", new DrinkItem(new Item.Settings().maxCount(16),
            3.0f, 100.0f, 25.0f, 0.0f, false, StatusEffects.SPEED, 30));
    public static final Item COFFEE = register("coffee", new DrinkItem(new Item.Settings().maxCount(16),
            4.0f, 10.0f, 35.0f, 12.0f, true, null, 0));

    // Preserved food. Thirst and stamina effects of eating are in Survival.
    public static final Item CANNED_BEANS = register("canned_beans", new TooltipItem(new Item.Settings().maxCount(16)
            .food(new FoodComponent.Builder().hunger(6).saturationModifier(0.7f).build())));
    public static final Item CANNED_SOUP = register("canned_soup", new TooltipItem(new Item.Settings().maxCount(16)
            .food(new FoodComponent.Builder().hunger(5).saturationModifier(0.5f).build())));
    public static final Item ENERGY_BAR = register("energy_bar", new TooltipItem(new Item.Settings().maxCount(32)
            .food(new FoodComponent.Builder().hunger(3).saturationModifier(0.4f).snack().build())));
    public static final Item MRE = register("mre", new TooltipItem(new Item.Settings().maxCount(8)
            .food(new FoodComponent.Builder().hunger(10).saturationModifier(0.9f).build())));

    public static final ItemGroup GROUP = Registry.register(Registries.ITEM_GROUP, DeadHorizon.id("main"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(WALKER_SPAWN_EGG))
                    .displayName(Text.translatable("itemGroup.deadhorizon"))
                    .entries((context, entries) -> {
                        entries.add(WALKER_SPAWN_EGG);
                        entries.add(RUNNER_SPAWN_EGG);
                        entries.add(BRUTE_SPAWN_EGG);
                        entries.add(SCREAMER_SPAWN_EGG);
                        entries.add(SPITTER_SPAWN_EGG);
                        entries.add(BLOATER_SPAWN_EGG);
                        entries.add(CRAWLER_SPAWN_EGG);
                        entries.add(SOLDIER_SPAWN_EGG);
                        entries.add(BANDAGE);
                        entries.add(FIRST_AID_KIT);
                        entries.add(PAINKILLERS);
                        entries.add(ANTIBIOTICS);
                        entries.add(ANTIDOTE);
                        entries.add(SPLINT);
                        entries.add(CLEAN_WATER);
                        entries.add(SODA);
                        entries.add(ENERGY_DRINK);
                        entries.add(COFFEE);
                        entries.add(CANNED_BEANS);
                        entries.add(CANNED_SOUP);
                        entries.add(ENERGY_BAR);
                        entries.add(MRE);
                        ModBlocks.ALL.forEach(entries::add);
                    })
                    .build());

    private ModItems() {
    }

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, DeadHorizon.id(name), item);
    }

    public static void register() {
    }
}
