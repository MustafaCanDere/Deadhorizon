package com.deadhorizon.stalker;

import com.deadhorizon.item.TooltipItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class BoltItem extends TooltipItem {
    public BoltItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW,
                SoundCategory.PLAYERS, 0.5f, 1.4f);
        if (!world.isClient) {
            BoltEntity bolt = new BoltEntity(world, user);
            bolt.setItem(stack.copyWithCount(1));
            bolt.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 1.0f, 1.0f);
            world.spawnEntity(bolt);
        }
        if (!user.getAbilities().creativeMode) stack.decrement(1);
        user.getItemCooldownManager().set(this, 10);
        return TypedActionResult.success(stack, world.isClient);
    }
}
