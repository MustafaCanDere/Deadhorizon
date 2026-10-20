package com.deadhorizon.item;

import com.deadhorizon.survival.Survival;
import com.deadhorizon.survival.SurvivalData;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.StewItem;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** A hot bowl from the cooking pot: food, plus some water, stamina and warmth. */
public class SoupItem extends StewItem {
    private final float thirst;
    private final float stamina;
    private final float warmth;

    public SoupItem(Settings settings, float thirst, float stamina, float warmth) {
        super(settings);
        this.thirst = thirst;
        this.stamina = stamina;
        this.warmth = warmth;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient && user instanceof PlayerEntity player) {
            if (thirst != 0) Survival.drink(player, thirst);
            if (stamina != 0) Survival.get(player).addStamina(stamina);
            if (warmth != 0) {
                SurvivalData data = Survival.get(player);
                data.temperature = Math.min(100.0f, data.temperature + warmth);
            }
        }
        return super.finishUsing(stack, world, user);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable(getTranslationKey() + ".tooltip").formatted(Formatting.GRAY));
    }
}
