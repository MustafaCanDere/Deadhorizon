package com.deadhorizon.item;

import com.deadhorizon.survival.Survival;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Something to drink: restores thirst and/or stamina, optionally with a short effect. */
public class DrinkItem extends Item {
    private final float thirst;
    private final float stamina;
    private final boolean returnsBottle;
    @Nullable
    private final StatusEffect effect;
    private final int effectSeconds;

    public DrinkItem(Settings settings, float thirst, float stamina, boolean returnsBottle,
                     @Nullable StatusEffect effect, int effectSeconds) {
        super(settings);
        this.thirst = thirst;
        this.stamina = stamina;
        this.returnsBottle = returnsBottle;
        this.effect = effect;
        this.effectSeconds = effectSeconds;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return ItemUsage.consumeHeldItem(world, user, hand);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient && user instanceof PlayerEntity player) {
            if (thirst != 0) Survival.drink(player, thirst);
            if (stamina != 0) Survival.get(player).addStamina(stamina);
            if (effect != null) player.addStatusEffect(new StatusEffectInstance(effect, effectSeconds * 20));
        }
        boolean creative = user instanceof PlayerEntity player && player.getAbilities().creativeMode;
        if (!creative) stack.decrement(1);
        if (returnsBottle && !creative) {
            if (stack.isEmpty()) return new ItemStack(Items.GLASS_BOTTLE);
            if (user instanceof PlayerEntity player && !player.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE))) {
                player.dropItem(new ItemStack(Items.GLASS_BOTTLE), false);
            }
        }
        return stack;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable(getTranslationKey() + ".tooltip").formatted(Formatting.GRAY));
    }
}
