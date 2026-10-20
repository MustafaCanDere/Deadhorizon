package com.deadhorizon.registry;

import com.deadhorizon.DeadHorizon;
import com.deadhorizon.item.DrinkItem;
import com.deadhorizon.item.MedicalItem;
import com.deadhorizon.item.MedicalItem.Treatment;
import com.deadhorizon.item.TooltipItem;
import com.deadhorizon.weapon.AttachmentItem;
import com.deadhorizon.weapon.FireAxeItem;
import com.deadhorizon.weapon.GrenadeItem;
import com.deadhorizon.weapon.GunItem;
import com.deadhorizon.weapon.GunSpec;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
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

    // Ammunition
    public static final Item AMMO_9MM = register("ammo_9mm", new TooltipItem(new Item.Settings().maxCount(64)));
    public static final Item AMMO_556 = register("ammo_556", new TooltipItem(new Item.Settings().maxCount(64)));
    public static final Item AMMO_762 = register("ammo_762", new TooltipItem(new Item.Settings().maxCount(64)));
    public static final Item AMMO_12G = register("ammo_12g", new TooltipItem(new Item.Settings().maxCount(32)));
    public static final Item AMMO_357 = register("ammo_357", new TooltipItem(new Item.Settings().maxCount(64)));

    // Firearms: GunSpec(ammo, magazine, damage, pellets, interval, automatic, spread, recoil, range, reloadTicks, noise, zoom)
    public static final Item PISTOL = register("pistol", new GunItem(gun(600),
            new GunSpec(() -> AMMO_9MM, 15, 6.0f, 1, 4, false, 2.0f, 2.5f, 48, 30, 40, 0.85f)));
    public static final Item SHOTGUN = register("shotgun", new GunItem(gun(400),
            new GunSpec(() -> AMMO_12G, 6, 3.5f, 8, 18, false, 7.0f, 9.0f, 24, 60, 56, 0.85f)));
    public static final Item HUNTING_RIFLE = register("hunting_rifle", new GunItem(gun(500),
            new GunSpec(() -> AMMO_762, 5, 20.0f, 1, 25, false, 0.4f, 7.0f, 128, 50, 72, 0.3f)));
    public static final Item AK47 = register("ak47", new GunItem(gun(800),
            new GunSpec(() -> AMMO_762, 30, 8.0f, 1, 2, true, 2.6f, 2.2f, 80, 45, 64, 0.75f)));
    public static final Item M4 = register("m4", new GunItem(gun(900),
            new GunSpec(() -> AMMO_556, 30, 7.0f, 1, 2, true, 1.6f, 1.6f, 90, 40, 64, 0.65f)));

    public static final Item MP5 = register("mp5", new GunItem(gun(700),
            new GunSpec(() -> AMMO_9MM, 30, 5.0f, 1, 2, true, 2.2f, 1.2f, 50, 35, 44, 0.8f)));
    public static final Item REVOLVER = register("revolver", new GunItem(gun(900),
            new GunSpec(() -> AMMO_357, 6, 11.0f, 1, 8, false, 1.2f, 6.0f, 60, 70, 56, 0.8f)));

    // Attachments: hold the gun in the off hand and use the part
    public static final Item SUPPRESSOR = register("suppressor",
            new AttachmentItem(new Item.Settings().maxCount(1), AttachmentItem.Type.SUPPRESSOR));
    public static final Item EXTENDED_MAG = register("extended_mag",
            new AttachmentItem(new Item.Settings().maxCount(1), AttachmentItem.Type.EXTENDED_MAG));
    public static final Item SCOPE = register("scope",
            new AttachmentItem(new Item.Settings().maxCount(1), AttachmentItem.Type.SCOPE));

    // Throwables
    public static final Item FRAG_GRENADE = register("frag_grenade",
            new GrenadeItem(new Item.Settings().maxCount(8), GrenadeItem.Kind.FRAG));
    public static final Item MOLOTOV = register("molotov",
            new GrenadeItem(new Item.Settings().maxCount(8), GrenadeItem.Kind.MOLOTOV));
    public static final Item PIPE_BOMB = register("pipe_bomb",
            new GrenadeItem(new Item.Settings().maxCount(8), GrenadeItem.Kind.PIPE_BOMB));

    // Melee
    public static final Item BASEBALL_BAT = register("baseball_bat",
            new SwordItem(ToolMaterials.WOOD, 5, -3.0f, new Item.Settings().maxDamage(180)));
    public static final Item NAIL_BAT = register("nail_bat",
            new SwordItem(ToolMaterials.IRON, 4, -3.0f, new Item.Settings().maxDamage(220)));
    public static final Item MACHETE = register("machete",
            new SwordItem(ToolMaterials.IRON, 3, -2.2f, new Item.Settings()));
    public static final Item CROWBAR = register("crowbar",
            new SwordItem(ToolMaterials.IRON, 4, -2.8f, new Item.Settings().maxDamage(600)));
    public static final Item KATANA = register("katana",
            new SwordItem(ToolMaterials.IRON, 5, -2.4f, new Item.Settings().maxDamage(400)));
    public static final Item FIRE_AXE = register("fire_axe",
            new FireAxeItem(ToolMaterials.IRON, 8.0f, -3.2f, new Item.Settings().maxDamage(450)));

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
                        entries.add(PISTOL);
                        entries.add(SHOTGUN);
                        entries.add(HUNTING_RIFLE);
                        entries.add(AK47);
                        entries.add(M4);
                        entries.add(MP5);
                        entries.add(REVOLVER);
                        entries.add(SUPPRESSOR);
                        entries.add(EXTENDED_MAG);
                        entries.add(SCOPE);
                        entries.add(FRAG_GRENADE);
                        entries.add(MOLOTOV);
                        entries.add(PIPE_BOMB);
                        entries.add(AMMO_9MM);
                        entries.add(AMMO_556);
                        entries.add(AMMO_762);
                        entries.add(AMMO_12G);
                        entries.add(AMMO_357);
                        entries.add(BASEBALL_BAT);
                        entries.add(NAIL_BAT);
                        entries.add(MACHETE);
                        entries.add(CROWBAR);
                        entries.add(KATANA);
                        entries.add(FIRE_AXE);
                        ModBlocks.ALL.forEach(entries::add);
                    })
                    .build());

    private ModItems() {
    }

    private static Item.Settings gun(int durability) {
        return new Item.Settings().maxCount(1).maxDamage(durability);
    }

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, DeadHorizon.id(name), item);
    }

    public static void register() {
    }
}
