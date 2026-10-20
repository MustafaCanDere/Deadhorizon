package com.deadhorizon.item;

import com.deadhorizon.infection.Infection;
import com.deadhorizon.registry.ModEffects;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Hold right-click to apply. What it does is set by its {@link Treatment}. */
public class MedicalItem extends Item {
    public record Treatment(int useTicks, float heal, boolean stopsBleeding, boolean slowsInfection,
                            boolean curesInfection, boolean curesPoison, int regenSeconds, boolean curesFracture) {
    }

    private final Treatment treatment;

    public MedicalItem(Settings settings, Treatment treatment) {
        super(settings);
        this.treatment = treatment;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return treatment.useTicks();
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient) {
            if (treatment.heal() > 0) user.heal(treatment.heal());
            if (treatment.stopsBleeding()) user.removeStatusEffect(ModEffects.BLEEDING);
            if (treatment.curesPoison()) user.removeStatusEffect(StatusEffects.POISON);
            if (treatment.curesFracture()
                    && (user.removeStatusEffect(ModEffects.FRACTURE) | user.removeStatusEffect(ModEffects.BROKEN_ARM))) {
                message(user, "message.deadhorizon.fracture.splinted", Formatting.GREEN);
            }
            if (treatment.regenSeconds() > 0) {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, treatment.regenSeconds() * 20));
            }
            if (treatment.curesInfection() && Infection.isInfected(user)) {
                Infection.cure(user);
                message(user, "message.deadhorizon.infection.cured", Formatting.GREEN);
            } else if (treatment.slowsInfection() && Infection.slow(user)) {
                message(user, "message.deadhorizon.infection.slowed", Formatting.AQUA);
            }
            world.playSound(null, user.getBlockPos(), SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, user.getSoundCategory(), 1.0f, 1.2f);
        }
        if (!(user instanceof PlayerEntity player && player.getAbilities().creativeMode)) {
            stack.decrement(1);
        }
        return stack;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable(getTranslationKey() + ".tooltip").formatted(Formatting.GRAY));
    }

    private static void message(LivingEntity user, String key, Formatting color) {
        if (user instanceof PlayerEntity player) {
            player.sendMessage(Text.translatable(key).formatted(color), true);
        }
    }
}
