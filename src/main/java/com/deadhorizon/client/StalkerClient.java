package com.deadhorizon.client;

import com.deadhorizon.block.AnomalyBlock;
import com.deadhorizon.registry.ModItems;
import com.deadhorizon.stalker.ArtifactItem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

/** Geiger counter clicks (when carried) and the anomaly/artifact detector beeps (when held). */
public final class StalkerClient {
    private static int beepCooldown;
    /** Distance to the nearest anomaly or artifact the detector last found, or -1. */
    public static double detected = -1;

    private StalkerClient() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(StalkerClient::tick);
    }

    private static void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.isPaused()) return;

        if (ClientSurvival.exposure > 0.02f && player.getInventory().contains(new ItemStack(ModItems.GEIGER_COUNTER))) {
            float chance = Math.min(0.9f, ClientSurvival.exposure * 0.35f);
            if (player.getRandom().nextFloat() < chance) {
                player.getWorld().playSound(player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON,
                        SoundCategory.PLAYERS, 0.35f, 1.8f + player.getRandom().nextFloat() * 0.4f, false);
            }
        }

        if (!player.isHolding(ModItems.DETECTOR)) {
            detected = -1;
            return;
        }
        if (player.age % 10 == 0) detected = scan(player);
        if (detected < 0) return;
        if (--beepCooldown <= 0) {
            beepCooldown = (int) Math.max(2, detected * 1.5);
            float pitch = (float) (2.0 - Math.min(1.2, detected / 12.0));
            player.getWorld().playSound(player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(),
                    SoundCategory.PLAYERS, 0.4f, pitch, false);
        }
    }

    private static double scan(ClientPlayerEntity player) {
        double best = -1;
        BlockPos center = player.getBlockPos();
        for (BlockPos pos : BlockPos.iterateOutwards(center, 10, 4, 10)) {
            if (player.getWorld().getBlockState(pos).getBlock() instanceof AnomalyBlock) {
                best = Math.sqrt(pos.getSquaredDistance(center));
                break;
            }
        }
        for (ItemEntity item : player.getWorld().getEntitiesByClass(ItemEntity.class, new Box(center).expand(16),
                e -> e.getStack().getItem() instanceof ArtifactItem)) {
            double d = item.distanceTo(player);
            if (best < 0 || d < best) best = d;
        }
        return best;
    }
}
