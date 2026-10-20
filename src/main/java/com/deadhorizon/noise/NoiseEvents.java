package com.deadhorizon.noise;

import com.deadhorizon.config.DHConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;

/** Hooks player actions that make noise. Sneaking players stay quiet. */
public final class NoiseEvents {
    private NoiseEvents() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 10 != 0) return;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.isSprinting() && !player.isSneaking() && !player.isSpectator()) {
                    NoiseSystem.emit(player.getServerWorld(), player.getPos(), DHConfig.INSTANCE.sprintNoiseRadius);
                }
            }
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world instanceof ServerWorld serverWorld && !player.isSneaking()) {
                NoiseSystem.emit(serverWorld, Vec3d.ofCenter(pos), DHConfig.INSTANCE.blockBreakNoiseRadius);
            }
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world instanceof ServerWorld serverWorld) {
                NoiseSystem.emit(serverWorld, player.getPos(), DHConfig.INSTANCE.attackNoiseRadius);
            }
            return ActionResult.PASS;
        });
    }
}
