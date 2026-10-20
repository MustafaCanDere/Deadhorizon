package com.deadhorizon.stalker;

import com.deadhorizon.item.TooltipItem;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** Anti-rad injection: takes half of the dose away at once, with a spell of sickness. */
public class RadMedItem extends TooltipItem {
    public RadMedItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            Radiation.reduce(user, 50.0f);
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0));
            world.playSound(null, user.getBlockPos(), SoundEvents.ITEM_HONEY_BOTTLE_DRINK, SoundCategory.PLAYERS, 0.6f, 1.6f);
            if (!user.getAbilities().creativeMode) stack.decrement(1);
            user.getItemCooldownManager().set(this, 100);
        }
        return TypedActionResult.success(stack, world.isClient);
    }
}
