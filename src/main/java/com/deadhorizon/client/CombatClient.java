package com.deadhorizon.client;

import com.deadhorizon.combat.Combat;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

/** Kick (V), dodge (left Alt + a direction) and the Dying Light ledge vault (hold jump against a 2-high wall). */
public final class CombatClient {
    private static KeyBinding kick;
    private static KeyBinding dodge;
    private static int kickCooldown, dodgeCooldown, vaultCooldown;

    private CombatClient() {
    }

    public static void register() {
        kick = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.deadhorizon.kick",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "category.deadhorizon"));
        dodge = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.deadhorizon.dodge",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "category.deadhorizon"));
        ClientTickEvents.END_CLIENT_TICK.register(CombatClient::tick);
    }

    private static boolean afford(ClientPlayerEntity player, float cost) {
        return player.isCreative() || (!ClientSurvival.exhausted && ClientSurvival.stamina >= cost);
    }

    private static void send(int action) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeByte(action);
        ClientPlayNetworking.send(Combat.ACTION, buf);
    }

    private static void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (kickCooldown > 0) kickCooldown--;
        if (dodgeCooldown > 0) dodgeCooldown--;
        if (vaultCooldown > 0) vaultCooldown--;
        if (player == null) return;

        while (kick.wasPressed()) {
            if (kickCooldown == 0 && afford(player, Combat.KICK_COST)) {
                kickCooldown = 16;
                send(Combat.KICK);
            }
        }
        while (dodge.wasPressed()) {
            if (dodgeCooldown == 0 && player.isOnGround() && afford(player, Combat.DODGE_COST)) {
                dodgeCooldown = 20;
                doDodge(player);
                send(Combat.DODGE);
            }
        }
        if (vaultCooldown == 0 && client.options.jumpKey.isPressed() && player.horizontalCollision
                && !player.isOnGround() && player.getVelocity().y < 0.2 && canVault(player)
                && afford(player, Combat.VAULT_COST)) {
            vaultCooldown = 15;
            Vec3d v = player.getVelocity();
            player.setVelocity(v.x, 0.62, v.z);
            send(Combat.VAULT);
        }
    }

    private static void doDodge(ClientPlayerEntity player) {
        float forward = player.input.movementForward;
        float sideways = player.input.movementSideways;
        if (forward == 0 && sideways == 0) forward = -1; // no direction: hop back
        Vec3d dir = new Vec3d(sideways, 0, forward).normalize().rotateY((float) Math.toRadians(-player.getYaw()));
        player.setVelocity(dir.x * 0.85, 0.25, dir.z * 0.85);
    }

    /** A wall at most two blocks high in front, with room to stand on top of it. */
    private static boolean canVault(ClientPlayerEntity player) {
        Direction facing = player.getHorizontalFacing();
        BlockPos feet = player.getBlockPos();
        BlockPos front = feet.offset(facing);
        var world = player.getWorld();
        for (int h = 1; h <= 2; h++) {
            BlockPos top = front.up(h);
            boolean wall = !world.getBlockState(front.up(h - 1)).getCollisionShape(world, front.up(h - 1)).isEmpty();
            boolean room = world.getBlockState(top).getCollisionShape(world, top).isEmpty()
                    && world.getBlockState(top.up()).getCollisionShape(world, top.up()).isEmpty();
            if (wall && room) return true;
        }
        return false;
    }
}
