package com.deadhorizon.block.entity;

import com.deadhorizon.block.CookingPotBlock;
import com.deadhorizon.food.Cooking;
import com.deadhorizon.noise.NoiseSystem;
import com.deadhorizon.registry.ModBlockEntities;
import com.deadhorizon.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CookingPotBlockEntity extends BlockEntity {
    /** Plain water needs a rolling boil to be safe; a stew takes longer. */
    private static final int BOIL_TICKS = 200;
    private static final int STEW_TICKS = 400;
    /** The smell of a cooked meal carries. */
    private static final double SMELL_RADIUS = 12.0;

    private final List<ItemStack> ingredients = new ArrayList<>();
    private boolean water;
    private int progress;
    @Nullable
    private Item result;
    private int servings;

    public CookingPotBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COOKING_POT, pos, state);
    }

    public List<ItemStack> ingredients() {
        return ingredients;
    }

    public static void tick(World world, BlockPos pos, BlockState state, CookingPotBlockEntity pot) {
        if (!pot.water || pot.servings > 0) return;
        if (!Cooking.hasHeat(world, pos)) {
            if (pot.progress > 0) {
                pot.progress = Math.max(0, pot.progress - 2); // cools off
            }
            return;
        }
        pot.progress++;
        if (pot.progress % 40 == 0) {
            world.playSound(null, pos, SoundEvents.BLOCK_BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundCategory.BLOCKS, 0.6f, 1.0f);
        }
        int needed = pot.ingredients.isEmpty() ? BOIL_TICKS : STEW_TICKS;
        if (pot.progress >= needed) {
            Cooking.Result cooked = Cooking.result(pot.ingredients);
            pot.ingredients.clear();
            pot.result = cooked.item();
            pot.servings = cooked.servings();
            pot.progress = 0;
            boolean stew = cooked.item() != ModItems.CLEAN_WATER;
            world.setBlockState(pos, state.with(CookingPotBlock.CONTENTS, stew ? 2 : 1), Block.NOTIFY_ALL);
            if (stew && world instanceof ServerWorld server) {
                NoiseSystem.emit(server, Vec3d.ofCenter(pos), SMELL_RADIUS);
            }
        }
        pot.markDirty();
    }

    public void use(PlayerEntity player, Hand hand) {
        ItemStack held = player.getStackInHand(hand);
        if (servings > 0 && result != null) {
            Item container = result == ModItems.CLEAN_WATER ? Items.GLASS_BOTTLE : Items.BOWL;
            if (!held.isOf(container)) {
                player.sendMessage(Text.translatable(container == Items.BOWL
                        ? "message.deadhorizon.pot_need_bowl" : "message.deadhorizon.pot_need_bottle"), true);
                return;
            }
            player.setStackInHand(hand, ItemUsage.exchangeStack(held, player, new ItemStack(result)));
            world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0f,
                    container == Items.BOWL ? 0.8f : 1.0f);
            if (--servings == 0) reset();
            markDirty();
            return;
        }
        if (!water && held.isOf(Items.WATER_BUCKET)) {
            fill(player, hand, new ItemStack(Items.BUCKET), SoundEvents.ITEM_BUCKET_EMPTY);
            return;
        }
        if (!water && held.isOf(Items.POTION) && PotionUtil.getPotion(held) == Potions.WATER) {
            fill(player, hand, new ItemStack(Items.GLASS_BOTTLE), SoundEvents.ITEM_BOTTLE_EMPTY);
            return;
        }
        if (water && Cooking.isIngredient(held) && ingredients.size() < Cooking.MAX_INGREDIENTS) {
            ingredients.add(held.copyWithCount(1));
            if (!player.getAbilities().creativeMode) held.decrement(1);
            world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 0.4f, 1.4f);
            progress = 0;
            markDirty();
            return;
        }
        if (held.isEmpty() && player.isSneaking() && !ingredients.isEmpty()) {
            player.setStackInHand(hand, ingredients.remove(ingredients.size() - 1));
            progress = 0;
            markDirty();
            return;
        }
        status(player);
    }

    private void fill(PlayerEntity player, Hand hand, ItemStack empty, SoundEvent sound) {
        ItemStack held = player.getStackInHand(hand);
        if (!player.getAbilities().creativeMode) player.setStackInHand(hand, ItemUsage.exchangeStack(held, player, empty));
        water = true;
        progress = 0;
        world.setBlockState(pos, getCachedState().with(CookingPotBlock.CONTENTS, 1), Block.NOTIFY_ALL);
        world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0f, 1.0f);
        markDirty();
    }

    private void status(PlayerEntity player) {
        String key;
        if (!water) key = "message.deadhorizon.pot_empty";
        else if (!Cooking.hasHeat(world, pos)) key = "message.deadhorizon.pot_no_heat";
        else key = "message.deadhorizon.pot_cooking";
        int needed = ingredients.isEmpty() ? BOIL_TICKS : STEW_TICKS;
        player.sendMessage(Text.translatable(key, ingredients.size(), Cooking.MAX_INGREDIENTS, progress * 100 / needed), true);
    }

    private void reset() {
        water = false;
        result = null;
        progress = 0;
        world.setBlockState(pos, getCachedState().with(CookingPotBlock.CONTENTS, 0), Block.NOTIFY_ALL);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        ingredients.clear();
        NbtList list = nbt.getList("Ingredients", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) ingredients.add(ItemStack.fromNbt(list.getCompound(i)));
        water = nbt.getBoolean("Water");
        progress = nbt.getInt("Progress");
        servings = nbt.getInt("Servings");
        result = nbt.contains("Result") ? Registries.ITEM.get(new Identifier(nbt.getString("Result"))) : null;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        NbtList list = new NbtList();
        for (ItemStack stack : ingredients) list.add(stack.writeNbt(new NbtCompound()));
        nbt.put("Ingredients", list);
        nbt.putBoolean("Water", water);
        nbt.putInt("Progress", progress);
        nbt.putInt("Servings", servings);
        if (result != null) nbt.putString("Result", Registries.ITEM.getId(result).toString());
    }
}
