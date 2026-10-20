package com.deadhorizon.mixin;

import com.deadhorizon.food.Spoilage;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Ages perishable food while it is being carried. */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void deadhorizon$spoil(World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        Spoilage.tick((ItemStack) (Object) this, world, entity, slot);
    }
}
