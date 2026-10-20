package com.deadhorizon.registry;

import com.deadhorizon.DeadHorizon;
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

    public static final ItemGroup GROUP = Registry.register(Registries.ITEM_GROUP, DeadHorizon.id("main"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(WALKER_SPAWN_EGG))
                    .displayName(Text.translatable("itemGroup.deadhorizon"))
                    .entries((context, entries) -> {
                        entries.add(WALKER_SPAWN_EGG);
                        entries.add(RUNNER_SPAWN_EGG);
                        entries.add(BRUTE_SPAWN_EGG);
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
