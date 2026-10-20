package com.deadhorizon.mixin;

import com.deadhorizon.config.DHConfig;
import com.deadhorizon.survival.Survival;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A vanilla water bottle is unboiled water; other potions wet the throat a little. */
@Mixin(PotionItem.class)
public abstract class PotionItemMixin {
    @Inject(method = "finishUsing", at = @At("HEAD"))
    private void deadhorizon$drink(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (world.isClient || !DHConfig.INSTANCE.thirstEnabled || !(user instanceof PlayerEntity player)) return;
        if (PotionUtil.getPotion(stack) == Potions.WATER) {
            Survival.drinkDirtyWater(player);
        } else {
            Survival.drink(player, 2.0f);
        }
    }
}
