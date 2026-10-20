package com.deadhorizon.food;

import com.deadhorizon.registry.ModItems;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;

/** Cooking pot recipes. Plain water boils clean; meat with vegetables makes the best stew. */
public final class Cooking {
    public static final int MAX_INGREDIENTS = 4;

    private static final Set<Item> VEGETABLES = Set.of(Items.CARROT, Items.POTATO, Items.BAKED_POTATO, Items.BEETROOT,
            Items.BROWN_MUSHROOM, Items.RED_MUSHROOM, Items.KELP, Items.DRIED_KELP, Items.WHEAT, Items.PUMPKIN);
    private static final Set<Item> MEAT = Set.of(Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.MUTTON, Items.RABBIT,
            Items.COD, Items.SALMON, Items.COOKED_BEEF, Items.COOKED_PORKCHOP, Items.COOKED_CHICKEN, Items.COOKED_MUTTON,
            Items.COOKED_RABBIT, Items.COOKED_COD, Items.COOKED_SALMON);

    private Cooking() {
    }

    public record Result(Item item, int servings) {
    }

    public static boolean isIngredient(ItemStack stack) {
        Item item = stack.getItem();
        return VEGETABLES.contains(item) || MEAT.contains(item) || item == Items.ROTTEN_FLESH
                || item == ModItems.CANNED_BEANS;
    }

    public static Result result(List<ItemStack> ingredients) {
        if (ingredients.isEmpty()) return new Result(ModItems.CLEAN_WATER, 3);
        int meat = 0, veg = 0;
        boolean rotten = false;
        for (ItemStack stack : ingredients) {
            Item item = stack.getItem();
            if (item == Items.ROTTEN_FLESH) rotten = true;
            else if (MEAT.contains(item) || item == ModItems.CANNED_BEANS) meat++;
            else if (VEGETABLES.contains(item)) veg++;
        }
        int servings = ingredients.size();
        if (rotten) return new Result(ModItems.GRUEL, servings);
        if (meat > 0 && veg > 0) return new Result(ModItems.MEAT_STEW, servings);
        if (veg >= 2) return new Result(ModItems.VEGETABLE_STEW, servings);
        return new Result(ModItems.GRUEL, servings);
    }

    /** A lit campfire, fire, lava, magma or a lit furnace directly under the pot. */
    public static boolean hasHeat(World world, BlockPos potPos) {
        BlockState below = world.getBlockState(potPos.down());
        if (CampfireBlock.isLitCampfire(below)) return true;
        if (below.isOf(Blocks.FIRE) || below.isOf(Blocks.SOUL_FIRE) || below.isOf(Blocks.LAVA) || below.isOf(Blocks.MAGMA_BLOCK)) {
            return true;
        }
        return below.getBlock() instanceof AbstractFurnaceBlock && below.get(AbstractFurnaceBlock.LIT);
    }
}
