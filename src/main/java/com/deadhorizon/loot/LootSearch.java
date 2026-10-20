package com.deadhorizon.loot;

import com.deadhorizon.block.entity.StorageBlockEntity;
import com.deadhorizon.noise.NoiseSystem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** Searching an unlooted container takes a moment, makes a little noise, then opens it. */
public final class LootSearch {
    private static final int SEARCH_TICKS = 30;
    private static final double SEARCH_NOISE = 6.0;
    private static final Map<UUID, Pending> PENDING = new HashMap<>();

    private record Pending(BlockPos pos, int doneAt) {
    }

    private LootSearch() {
    }

    public static void start(ServerPlayerEntity player, BlockPos pos) {
        if (PENDING.containsKey(player.getUuid())) return;
        PENDING.put(player.getUuid(), new Pending(pos.toImmutable(), player.getServer().getTicks() + SEARCH_TICKS));
        player.sendMessage(Text.translatable("message.deadhorizon.searching").formatted(Formatting.GRAY), true);
        player.getServerWorld().playSound(null, pos, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, SoundCategory.BLOCKS, 1.0f, 0.7f);
        NoiseSystem.emit(player.getServerWorld(), Vec3d.ofCenter(pos), SEARCH_NOISE);
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            int now = server.getTicks();
            Iterator<Map.Entry<UUID, Pending>> it = PENDING.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, Pending> entry = it.next();
                Pending pending = entry.getValue();
                if (now < pending.doneAt()) continue;
                it.remove();
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
                if (player == null || player.isDead()) continue;
                if (player.squaredDistanceTo(Vec3d.ofCenter(pending.pos())) > 36.0) continue;
                if (player.getServerWorld().getBlockEntity(pending.pos()) instanceof StorageBlockEntity storage) {
                    player.openHandledScreen(storage);
                }
            }
        });
    }
}
