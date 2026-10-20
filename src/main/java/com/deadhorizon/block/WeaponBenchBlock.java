package com.deadhorizon.block;

import com.deadhorizon.weapon.GunItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ToolItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Use it holding a worn gun or melee weapon: one iron ingot restores a quarter of its durability and clears a jam. */
public class WeaponBenchBlock extends FurnitureBlock {
    public WeaponBenchBlock(Settings settings, double[]... boxes) {
        super(settings, boxes);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack held = player.getStackInHand(hand);
        boolean weapon = held.getItem() instanceof GunItem || held.getItem() instanceof ToolItem;
        if (!weapon || !held.isDamageable()) return ActionResult.PASS;
        if (world.isClient) return ActionResult.SUCCESS;
        if (!held.isDamaged() && !GunItem.isJammed(held)) {
            player.sendMessage(Text.translatable("message.deadhorizon.bench_fine"), true);
            return ActionResult.CONSUME;
        }
        if (!player.getAbilities().creativeMode && !takeIron(player.getInventory())) {
            player.sendMessage(Text.translatable("message.deadhorizon.bench_no_iron"), true);
            return ActionResult.CONSUME;
        }
        held.setDamage(Math.max(0, held.getDamage() - held.getMaxDamage() / 4));
        if (held.getItem() instanceof GunItem) GunItem.setJammed(held, false);
        world.playSound(null, pos, SoundEvents.BLOCK_SMITHING_TABLE_USE, SoundCategory.BLOCKS, 1.0f, 1.0f);
        player.sendMessage(Text.translatable("message.deadhorizon.bench_repaired",
                100 - held.getDamage() * 100 / held.getMaxDamage()), true);
        return ActionResult.CONSUME;
    }

    private static boolean takeIron(PlayerInventory inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isOf(Items.IRON_INGOT)) {
                stack.decrement(1);
                return true;
            }
        }
        return false;
    }
}
