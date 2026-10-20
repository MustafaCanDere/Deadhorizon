package com.deadhorizon.weapon;

import com.deadhorizon.entity.projectile.GrenadeEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Something to throw: a frag grenade, a molotov or a beeping pipe bomb that lures zombies in. */
public class GrenadeItem extends Item {
    public enum Kind { FRAG, MOLOTOV, PIPE_BOMB }

    private final Kind kind;

    public GrenadeItem(Settings settings, Kind kind) {
        super(settings);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW,
                SoundCategory.PLAYERS, 0.6f, 0.5f);
        if (!world.isClient) {
            GrenadeEntity grenade = new GrenadeEntity(world, user);
            grenade.setItem(stack.copyWithCount(1));
            grenade.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 1.1f, 1.0f);
            world.spawnEntity(grenade);
        }
        user.getItemCooldownManager().set(this, 20);
        if (!user.getAbilities().creativeMode) stack.decrement(1);
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable(getTranslationKey() + ".tooltip").formatted(Formatting.GRAY));
    }
}
