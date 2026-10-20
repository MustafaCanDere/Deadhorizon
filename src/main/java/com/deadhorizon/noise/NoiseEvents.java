package com.deadhorizon.noise;

import com.deadhorizon.config.DHConfig;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;

/** Hooks player actions that make noise. Sneaking players stay quiet, mostly: breaking glass is never quiet. */
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
            if (!(world instanceof ServerWorld serverWorld)) return;
            if (state.getSoundGroup() == BlockSoundGroup.GLASS) {
                NoiseSystem.emit(serverWorld, Vec3d.ofCenter(pos), DHConfig.INSTANCE.blockBreakNoiseRadius * 2.0);
            } else if (!player.isSneaking()) {
                NoiseSystem.emit(serverWorld, Vec3d.ofCenter(pos), DHConfig.INSTANCE.blockBreakNoiseRadius);
            }
        });

        // Doors, trapdoors and gates creak; easing them open while sneaking halves it.
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world instanceof ServerWorld serverWorld && !player.isSpectator()) {
                BlockState state = world.getBlockState(hitResult.getBlockPos());
                if (state.getBlock() instanceof DoorBlock || state.getBlock() instanceof TrapdoorBlock
                        || state.getBlock() instanceof FenceGateBlock) {
                    double radius = DHConfig.INSTANCE.doorNoiseRadius * (player.isSneaking() ? 0.5 : 1.0);
                    NoiseSystem.emit(serverWorld, Vec3d.ofCenter(hitResult.getBlockPos()), radius);
                }
            }
            return ActionResult.PASS;
        });

        // A hard landing is loud.
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayerEntity player && source.isIn(DamageTypeTags.IS_FALL)) {
                NoiseSystem.emit(player.getServerWorld(), player.getPos(), DHConfig.INSTANCE.landingNoiseRadius);
            }
            return true;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world instanceof ServerWorld serverWorld) {
                NoiseSystem.emit(serverWorld, player.getPos(), DHConfig.INSTANCE.attackNoiseRadius);
            }
            return ActionResult.PASS;
        });
    }
}
