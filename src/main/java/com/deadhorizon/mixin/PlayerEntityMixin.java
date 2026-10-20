package com.deadhorizon.mixin;

import com.deadhorizon.survival.Survival;
import com.deadhorizon.survival.SurvivalData;
import com.deadhorizon.survival.SurvivalHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Gives every player thirst and stamina, saved with the player, and hooks eating. */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements SurvivalHolder {
    @Unique
    private final SurvivalData deadhorizon$survival = new SurvivalData();

    @Override
    public SurvivalData deadhorizon$getSurvival() {
        return deadhorizon$survival;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void deadhorizon$write(NbtCompound nbt, CallbackInfo ci) {
        nbt.put("DeadHorizonSurvival", deadhorizon$survival.toNbt());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void deadhorizon$read(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("DeadHorizonSurvival")) {
            deadhorizon$survival.fromNbt(nbt.getCompound("DeadHorizonSurvival"));
        }
    }

    @Inject(method = "eatFood", at = @At("HEAD"))
    private void deadhorizon$eat(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (!world.isClient && stack.isFood()) {
            Survival.onConsume((PlayerEntity) (Object) this, stack);
        }
    }
}
