package com.deadhorizon.client;

import com.deadhorizon.weapon.AttachmentItem;
import com.deadhorizon.weapon.GunItem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

/**
 * First-person gun animation: aiming down the sights, a low-ready carry while sprinting, recoil kick,
 * a magazine change while reloading and a tilt to inspect a jam. All of it is a transform on top of the
 * vanilla held-item placement, applied from {@code HeldItemRendererMixin}.
 */
public final class GunAnimation {
    private static final Identifier SCOPE = new Identifier("textures/misc/spyglass_scope.png");

    private static float aim, prevAim;
    private static float sprint, prevSprint;
    private static float kick;
    private static long shotTick = -100L;
    private static long ticks;

    private GunAnimation() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ticks++;
            prevAim = aim;
            prevSprint = sprint;
            ClientPlayerEntity player = client.player;
            boolean gun = player != null && player.getMainHandStack().getItem() instanceof GunItem;
            boolean reloading = gun && GunItem.reloadUntil(player.getMainHandStack()) > 0;
            aim = approach(aim, gun && player.isSneaking() && !player.isSprinting() && !reloading ? 1.0f : 0.0f, 0.25f);
            sprint = approach(sprint, gun && player.isSprinting() ? 1.0f : 0.0f, 0.2f);
        });
        HudRenderCallback.EVENT.register(GunAnimation::drawScope);
    }

    /** Called when the server confirms a shot (with the recoil packet). */
    public static void onShot(float recoil) {
        shotTick = ticks;
        kick = MathHelper.clamp(recoil / 4.0f, 0.3f, 1.5f);
    }

    private static float approach(float value, float target, float step) {
        return value < target ? Math.min(target, value + step) : Math.max(target, value - step);
    }

    /** True while looking through a scope: the gun model is hidden and the scope overlay drawn instead. */
    public static boolean scoped(float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !client.options.getPerspective().isFirstPerson()) return false;
        ItemStack stack = client.player.getMainHandStack();
        return stack.getItem() instanceof GunItem && GunItem.has(stack, AttachmentItem.Type.SCOPE)
                && MathHelper.lerp(tickDelta, prevAim, aim) > 0.9f;
    }

    public static void apply(AbstractClientPlayerEntity player, Hand hand, ItemStack stack, float tickDelta, MatrixStack matrices) {
        if (hand != Hand.MAIN_HAND || !(stack.getItem() instanceof GunItem gun)) return;
        Arm arm = player.getMainArm();
        float side = arm == Arm.RIGHT ? 1.0f : -1.0f;

        // Aim down the sights: pull the gun to the centre of the screen and up to the eye.
        float a = smooth(MathHelper.lerp(tickDelta, prevAim, aim));
        matrices.translate(-0.56f * side * a, 0.2f * a, 0.15f * a);

        // Sprinting: muzzle down and across the body.
        float s = smooth(MathHelper.lerp(tickDelta, prevSprint, sprint));
        matrices.translate(-0.1f * side * s, -0.08f * s, 0.0f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(40.0f * side * s));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-20.0f * s));

        // Recoil: a sharp kick back and up that settles over a few ticks.
        float since = ticks - shotTick + tickDelta;
        if (since < 8.0f) {
            float k = kick * (float) Math.exp(-since * 0.7f) * (1.0f - a * 0.4f);
            matrices.translate(0.0f, 0.02f * k, 0.14f * k);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(10.0f * k));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(2.0f * side * k));
        }

        // Reloading: tip the gun over, drop the magazine out, seat the new one, rack it.
        long until = GunItem.reloadUntil(stack);
        if (until > 0 && player.getWorld() != null) {
            float total = gun.spec().reloadTicks();
            float left = until - player.getWorld().getTime() - tickDelta;
            float p = MathHelper.clamp(1.0f - left / total, 0.0f, 1.0f);
            float tilt = MathHelper.sin(p * MathHelper.PI);
            float seat = p > 0.55f && p < 0.75f ? MathHelper.sin((p - 0.55f) / 0.2f * MathHelper.PI) : 0.0f;
            float rack = p > 0.8f ? MathHelper.sin((p - 0.8f) / 0.2f * MathHelper.PI) : 0.0f;
            matrices.translate(0.0f, -0.18f * tilt + 0.05f * seat, -0.05f * rack);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-25.0f * tilt));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(35.0f * side * tilt));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-6.0f * side * rack));
        }

        // A jam: hold it tilted, ejection port up, until R clears it.
        if (GunItem.isJammed(stack)) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-30.0f * side));
            matrices.translate(0.0f, -0.05f, 0.0f);
        }
    }

    private static float smooth(float t) {
        return t * t * (3.0f - 2.0f * t);
    }

    private static void drawScope(DrawContext ctx, float tickDelta) {
        if (!scoped(tickDelta)) return;
        int w = ctx.getScaledWindowWidth();
        int h = ctx.getScaledWindowHeight();
        int size = Math.min(w, h);
        int x = (w - size) / 2;
        int y = (h - size) / 2;
        ctx.drawTexture(SCOPE, x, y, -90, 0.0f, 0.0f, size, size, size, size);
        ctx.fill(0, 0, w, y, 0xFF000000);
        ctx.fill(0, y + size, w, h, 0xFF000000);
        ctx.fill(0, y, x, y + size, 0xFF000000);
        ctx.fill(x + size, y, w, y + size, 0xFF000000);
        ctx.fill(w / 2, y + size / 8, w / 2 + 1, y + size * 7 / 8, 0xCC000000);   // crosshair
        ctx.fill(x + size / 8, h / 2, x + size * 7 / 8, h / 2 + 1, 0xCC000000);
    }
}
