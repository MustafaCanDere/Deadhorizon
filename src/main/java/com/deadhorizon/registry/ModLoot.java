package com.deadhorizon.registry;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.entry.EmptyEntry;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.function.SetDamageLootFunction;
import net.minecraft.loot.function.SetNbtLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.util.Identifier;

import java.util.Set;

/** Puts medical supplies, survival food and weapons into vanilla chests until our own structures arrive. */
public final class ModLoot {
    private static final Set<Identifier> COMMON = Set.of(
            LootTables.VILLAGE_PLAINS_CHEST, LootTables.VILLAGE_SAVANNA_HOUSE_CHEST,
            LootTables.VILLAGE_SNOWY_HOUSE_CHEST, LootTables.VILLAGE_TAIGA_HOUSE_CHEST,
            LootTables.VILLAGE_DESERT_HOUSE_CHEST, LootTables.SIMPLE_DUNGEON_CHEST,
            LootTables.ABANDONED_MINESHAFT_CHEST, LootTables.SHIPWRECK_SUPPLY_CHEST);
    private static final Set<Identifier> RARE = Set.of(
            LootTables.PILLAGER_OUTPOST_CHEST, LootTables.STRONGHOLD_CORRIDOR_CHEST,
            LootTables.DESERT_PYRAMID_CHEST, LootTables.JUNGLE_TEMPLE_CHEST,
            LootTables.WOODLAND_MANSION_CHEST, LootTables.ANCIENT_CITY_CHEST);

    private ModLoot() {
    }

    public static void register() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (!source.isBuiltin()) return;
            if (COMMON.contains(id)) {
                tableBuilder.pool(LootPool.builder()
                        .rolls(UniformLootNumberProvider.create(0, 2))
                        .with(entry(ModItems.BANDAGE, 10))
                        .with(entry(ModItems.PAINKILLERS, 6))
                        .with(entry(ModItems.ANTIBIOTICS, 3))
                        .with(entry(ModItems.FIRST_AID_KIT, 1))
                        .with(EmptyEntry.builder().weight(10)));
                tableBuilder.pool(LootPool.builder()
                        .rolls(UniformLootNumberProvider.create(0, 2))
                        .with(entry(ModItems.CANNED_BEANS, 6))
                        .with(entry(ModItems.CANNED_SOUP, 5))
                        .with(entry(ModItems.CLEAN_WATER, 6))
                        .with(entry(ModItems.SODA, 3))
                        .with(entry(ModItems.ENERGY_BAR, 3))
                        .with(entry(ModItems.SPLINT, 2))
                        .with(EmptyEntry.builder().weight(8)));
                tableBuilder.pool(LootPool.builder()
                        .rolls(UniformLootNumberProvider.create(0, 1))
                        .with(ammo(ModItems.AMMO_9MM, 4, 3, 10))
                        .with(ammo(ModItems.AMMO_12G, 3, 1, 5))
                        .with(entry(ModItems.BASEBALL_BAT, 2))
                        .with(entry(ModItems.MACHETE, 1))
                        .with(EmptyEntry.builder().weight(14)));
            } else if (RARE.contains(id)) {
                tableBuilder.pool(LootPool.builder()
                        .rolls(UniformLootNumberProvider.create(1, 2))
                        .with(entry(ModItems.ANTIBIOTICS, 6))
                        .with(entry(ModItems.FIRST_AID_KIT, 4))
                        .with(entry(ModItems.ANTIDOTE, 2))
                        .with(entry(ModItems.MRE, 4))
                        .with(entry(ModItems.ENERGY_DRINK, 3)));
                tableBuilder.pool(LootPool.builder()
                        .rolls(UniformLootNumberProvider.create(0, 1))
                        .with(gun(ModItems.PISTOL, 5, 6))
                        .with(gun(ModItems.SHOTGUN, 3, 2))
                        .with(gun(ModItems.HUNTING_RIFLE, 2, 2))
                        .with(gun(ModItems.AK47, 1, 10))
                        .with(ammo(ModItems.AMMO_762, 4, 3, 12))
                        .with(ammo(ModItems.AMMO_556, 3, 3, 12))
                        .with(entry(ModItems.KATANA, 1))
                        .with(EmptyEntry.builder().weight(6)));
            }
        });
    }

    private static LeafEntry.Builder<?> entry(Item item, int weight) {
        return ItemEntry.builder(item).weight(weight);
    }

    private static LeafEntry.Builder<?> ammo(Item item, int weight, int min, int max) {
        return ItemEntry.builder(item).weight(weight)
                .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(min, max)));
    }

    /** A found gun is worn and only partly loaded. */
    private static LeafEntry.Builder<?> gun(Item item, int weight, int rounds) {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("Ammo", rounds);
        return ItemEntry.builder(item).weight(weight)
                .apply(SetDamageLootFunction.builder(UniformLootNumberProvider.create(0.3f, 0.9f)))
                .apply(SetNbtLootFunction.builder(nbt));
    }
}
