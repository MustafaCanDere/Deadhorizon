package com.deadhorizon.client;

import com.deadhorizon.weapon.GunItem;
import com.deadhorizon.weapon.Guns;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

/** Reload / fire mode keys, recoil kick and the ammo counter. */
public final class GunClient {
    private static KeyBinding reload;
    private static KeyBinding fireMode;

    private GunClient() {
    }

    public static void register() {
        reload = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.deadhorizon.reload",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.deadhorizon"));
        fireMode = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.deadhorizon.fire_mode",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, "category.deadhorizon"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (reload.wasPressed()) ClientPlayNetworking.send(Guns.RELOAD, PacketByteBufs.empty());
            while (fireMode.wasPressed()) ClientPlayNetworking.send(Guns.FIRE_MODE, PacketByteBufs.empty());
        });

        ClientPlayNetworking.registerGlobalReceiver(Guns.RECOIL, (client, handler, buf, sender) -> {
            float pitch = buf.readFloat();
            float yaw = buf.readFloat();
            client.execute(() -> {
                ClientPlayerEntity player = client.player;
                if (player == null) return;
                player.setPitch(Math.max(-90.0f, player.getPitch() - pitch));
                player.setYaw(player.getYaw() + yaw);
                GunAnimation.onShot(pitch);
            });
        });

        HudRenderCallback.EVENT.register(GunClient::drawAmmo);
    }

    private static void drawAmmo(DrawContext ctx, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.options.hudHidden) return;
        ItemStack stack = player.getMainHandStack();
        if (!(stack.getItem() instanceof GunItem gun)) return;
        int loaded = GunItem.ammo(stack);
        int reserve = Guns.count(player.getInventory(), gun.spec().ammo().get());
        Text text;
        if (GunItem.isJammed(stack)) {
            text = Text.translatable("hud.deadhorizon.gun.jammed").formatted(Formatting.RED);
        } else if (GunItem.reloadUntil(stack) > 0) {
            text = Text.translatable("hud.deadhorizon.gun.reloading").formatted(Formatting.YELLOW);
        } else {
            Formatting color = loaded == 0 ? Formatting.RED : loaded <= gun.magazine(stack) / 4 ? Formatting.GOLD : Formatting.WHITE;
            MutableText counter = Text.literal(loaded + " / " + reserve).formatted(color);
            if (gun.spec().automatic()) {
                counter.append(Text.literal("  ")).append(Text.translatable(gun.isAuto(stack)
                        ? "hud.deadhorizon.gun.auto" : "hud.deadhorizon.gun.semi").formatted(Formatting.GRAY));
            }
            text = counter;
        }
        int x = ctx.getScaledWindowWidth() / 2 + 98;
        int y = ctx.getScaledWindowHeight() - 14;
        ctx.drawTextWithShadow(client.textRenderer, text, x, y, 0xFFFFFF);
    }
}
