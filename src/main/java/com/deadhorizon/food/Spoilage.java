package com.deadhorizon.food;

import com.deadhorizon.config.DHConfig;
import com.deadhorizon.registry.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

/**
 * Fresh food goes off. A perishable stack is stamped with the world time it was made (rounded to a quarter day
 * so stacks from the same trip still merge) and turns into rotten flesh once past its shelf life.
 * Cans, rations and dried food never spoil; a fridge slows it right down (see StorageBlockEntity).
 */
public final class Spoilage {
    public static final String MADE = "DHMade";
    private static final long DAY = 24000L;
    private static final long STAMP_STEP = DAY / 4;
    private static final Map<Item, Float> SHELF_DAYS = new HashMap<>();

    static {
        for (Item raw : new Item[]{Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.MUTTON, Items.RABBIT,
                Items.COD, Items.SALMON, Items.TROPICAL_FISH}) {
            SHELF_DAYS.put(raw, 2.0f);
        }
        for (Item cooked : new Item[]{Items.COOKED_BEEF, Items.COOKED_PORKCHOP, Items.COOKED_CHICKEN, Items.COOKED_MUTTON,
                Items.COOKED_RABBIT, Items.COOKED_COD, Items.COOKED_SALMON}) {
            SHELF_DAYS.put(cooked, 4.0f);
        }
        SHELF_DAYS.put(Items.MILK_BUCKET, 2.0f);
        SHELF_DAYS.put(Items.MUSHROOM_STEW, 3.0f);
        SHELF_DAYS.put(Items.BEETROOT_SOUP, 3.0f);
        SHELF_DAYS.put(Items.RABBIT_STEW, 3.0f);
        SHELF_DAYS.put(Items.MELON_SLICE, 3.0f);
        SHELF_DAYS.put(Items.PUMPKIN_PIE, 4.0f);
        SHELF_DAYS.put(Items.BAKED_POTATO, 4.0f);
        SHELF_DAYS.put(Items.SWEET_BERRIES, 4.0f);
        SHELF_DAYS.put(Items.GLOW_BERRIES, 4.0f);
        SHELF_DAYS.put(Items.BREAD, 5.0f);
        SHELF_DAYS.put(Items.COOKIE, 6.0f);
        SHELF_DAYS.put(Items.APPLE, 7.0f);
        SHELF_DAYS.put(Items.CARROT, 8.0f);
        SHELF_DAYS.put(Items.BEETROOT, 8.0f);
        SHELF_DAYS.put(Items.POTATO, 10.0f);
        SHELF_DAYS.put(ModItems.MEAT_STEW, 2.0f);
        SHELF_DAYS.put(ModItems.VEGETABLE_STEW, 3.0f);
        SHELF_DAYS.put(ModItems.GRUEL, 3.0f);
    }

    private Spoilage() {
    }

    public static boolean isPerishable(ItemStack stack) {
        return SHELF_DAYS.containsKey(stack.getItem());
    }

    public static long shelfTicks(ItemStack stack) {
        Float days = SHELF_DAYS.get(stack.getItem());
        return days == null ? Long.MAX_VALUE : (long) (days * DAY * DHConfig.INSTANCE.foodShelfLifeMultiplier);
    }

    /** 0 = just made, 1 = about to rot; -1 if the stack is not perishable or not stamped yet. */
    public static float age(ItemStack stack, long now) {
        NbtCompound nbt = stack.getNbt();
        if (!isPerishable(stack) || nbt == null || !nbt.contains(MADE)) return -1.0f;
        return Math.min(1.0f, (float) (now - nbt.getLong(MADE)) / shelfTicks(stack));
    }

    /** Moves a stack's stamp forward, i.e. makes it fresher by that many ticks. */
    public static void preserve(ItemStack stack, long ticks) {
        NbtCompound nbt = stack.getNbt();
        if (ticks > 0 && nbt != null && nbt.contains(MADE)) {
            nbt.putLong(MADE, nbt.getLong(MADE) + ticks);
        }
    }

    /** Called from ItemStack#inventoryTick for stacks carried by an entity. */
    public static void tick(ItemStack stack, World world, Entity holder, int slot) {
        if (world.isClient || !DHConfig.INSTANCE.foodSpoilageEnabled || !isPerishable(stack)) return;
        if (holder instanceof PlayerEntity player && player.isCreative()) return;
        long now = world.getTime();
        NbtCompound nbt = stack.getOrCreateNbt();
        if (!nbt.contains(MADE)) {
            nbt.putLong(MADE, now - now % STAMP_STEP);
            return;
        }
        if (now - nbt.getLong(MADE) < shelfTicks(stack) || !(holder instanceof PlayerEntity player)) return;

        ItemStack rotten = new ItemStack(Items.ROTTEN_FLESH, stack.getCount());
        PlayerInventory inventory = player.getInventory();
        if (slot < inventory.main.size() && inventory.main.get(slot) == stack) {
            inventory.main.set(slot, rotten);
        } else if (inventory.offHand.get(0) == stack) {
            inventory.offHand.set(0, rotten);
        } else {
            return;
        }
        player.sendMessage(Text.translatable("message.deadhorizon.spoiled", stack.getName()).formatted(Formatting.DARK_GREEN), true);
    }
}
