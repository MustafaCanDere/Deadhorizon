package com.deadhorizon.mixin.client;

import com.deadhorizon.weapon.GunItem;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Third person: a gun is held up in both hands, shouldered like a crossbow, and lowered while reloading. */
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true, require = 0)
    private static void deadhorizon$gunPose(AbstractClientPlayerEntity player, Hand hand,
                                            CallbackInfoReturnable<BipedEntityModel.ArmPose> cir) {
        ItemStack stack = player.getStackInHand(hand);
        if (hand == Hand.MAIN_HAND && stack.getItem() instanceof GunItem) {
            if (player.isSprinting()) return;
            cir.setReturnValue(GunItem.reloadUntil(stack) > 0
                    ? BipedEntityModel.ArmPose.CROSSBOW_CHARGE : BipedEntityModel.ArmPose.CROSSBOW_HOLD);
        } else if (hand == Hand.OFF_HAND && player.getMainHandStack().getItem() instanceof GunItem && stack.isEmpty()) {
            cir.setReturnValue(BipedEntityModel.ArmPose.EMPTY);
        }
    }
}
