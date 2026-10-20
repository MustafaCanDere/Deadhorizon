package com.deadhorizon.stalker;

import com.deadhorizon.item.DrinkItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** The Zone's oldest remedy: warms you, flushes a little radiation, and makes the world spin. */
public class VodkaItem extends DrinkItem {
    public VodkaItem(Settings settings) {
        super(settings, 1.0f, 0.0f, -5.0f, 10.0f, true, StatusEffects.NAUSEA, 12);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient && user instanceof PlayerEntity player) Radiation.reduce(player, 15.0f);
        return super.finishUsing(stack, world, user);
    }
}
