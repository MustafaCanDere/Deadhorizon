package com.deadhorizon.mixin;

import com.deadhorizon.registry.ModEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MilkBucketItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/** Milk must not cure the infection or stop bleeding, so those effects survive drinking it. */
@Mixin(MilkBucketItem.class)
public abstract class MilkBucketItemMixin {
    @Unique
    private static final ThreadLocal<List<StatusEffectInstance>> deadhorizon$kept = ThreadLocal.withInitial(ArrayList::new);

    @Inject(method = "finishUsing", at = @At("HEAD"))
    private void deadhorizon$remember(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        List<StatusEffectInstance> kept = deadhorizon$kept.get();
        kept.clear();
        if (world.isClient) return;
        for (StatusEffect effect : new StatusEffect[]{ModEffects.INFECTION, ModEffects.BLEEDING}) {
            StatusEffectInstance instance = user.getStatusEffect(effect);
            if (instance != null) kept.add(new StatusEffectInstance(instance));
        }
    }

    @Inject(method = "finishUsing", at = @At("RETURN"))
    private void deadhorizon$restore(ItemStack stack, World world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        List<StatusEffectInstance> kept = deadhorizon$kept.get();
        for (StatusEffectInstance instance : kept) {
            user.addStatusEffect(instance);
        }
        kept.clear();
    }
}
