package com.deadhorizon.item;

import com.deadhorizon.event.Apocalypse;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** A hand-held radio: tells you what day it is, when the next Blood Moon rises and where supplies came down. */
public class RadioItem extends TooltipItem {
    public RadioItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && world.getServer() != null) {
            world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), SoundCategory.PLAYERS, 0.5f, 0.6f);
            user.sendMessage(Apocalypse.status(world.getServer()).copy().formatted(Formatting.GOLD), false);
        }
        user.getItemCooldownManager().set(this, 40);
        return TypedActionResult.success(user.getStackInHand(hand), world.isClient);
    }
}
