package com.deadhorizon.event;

import com.deadhorizon.DeadHorizon;
import com.deadhorizon.block.entity.StorageBlockEntity;
import com.deadhorizon.config.DHConfig;
import com.deadhorizon.entity.zombie.DHZombieEntity;
import com.deadhorizon.noise.NoiseSystem;
import com.deadhorizon.registry.ModBlocks;
import com.deadhorizon.registry.ModEntities;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.List;

/**
 * The world getting worse: a day counter, zombies that grow stronger, a Blood Moon every few nights,
 * wandering hordes and radio-announced supply drops.
 */
public final class Apocalypse {
    public static final Identifier BLOOD_MOON_SYNC = DeadHorizon.id("blood_moon");
    private static final Identifier SUPPLY_LOOT = DeadHorizon.id("containers/supply_drop");

    private Apocalypse() {
    }

    /** Days survived, counting from 1. */
    public static long day(ServerWorld world) {
        return world.getTimeOfDay() / 24000L + 1;
    }

    public static boolean isBloodMoonDay(long day) {
        int every = DHConfig.INSTANCE.bloodMoonEveryDays;
        return every > 0 && day % every == 0;
    }

    private static boolean isNight(ServerWorld world) {
        long time = world.getTimeOfDay() % 24000L;
        return time >= 13000L && time < 23000L;
    }

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getRegistryKey() != World.OVERWORLD || world.getTime() % 20 != 0) return;
            tick(world);
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerWorld overworld = server.getOverworld();
            syncBloodMoon(handler.player, ApocalypseState.get(overworld).bloodMoon);
        });
    }

    private static void tick(ServerWorld world) {
        DHConfig cfg = DHConfig.INSTANCE;
        ApocalypseState state = ApocalypseState.get(world);
        long day = day(world);
        List<ServerPlayerEntity> players = world.getPlayers(p -> !p.isSpectator() && !p.isCreative());

        if (day != state.lastAnnouncedDay) {
            state.lastAnnouncedDay = day;
            state.markDirty();
            for (ServerPlayerEntity player : world.getPlayers()) {
                player.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("title.deadhorizon.day", day)
                        .formatted(Formatting.DARK_RED)));
                if (isBloodMoonDay(day)) {
                    player.networkHandler.sendPacket(new SubtitleS2CPacket(
                            Text.translatable("title.deadhorizon.blood_moon_tonight").formatted(Formatting.RED)));
                }
            }
        }

        boolean bloodMoon = isBloodMoonDay(day) && isNight(world);
        if (bloodMoon != state.bloodMoon) {
            state.bloodMoon = bloodMoon;
            state.markDirty();
            for (ServerPlayerEntity player : world.getServer().getPlayerManager().getPlayerList()) {
                syncBloodMoon(player, bloodMoon);
                player.sendMessage(Text.translatable(bloodMoon ? "message.deadhorizon.blood_moon.start"
                        : "message.deadhorizon.blood_moon.end").formatted(Formatting.DARK_RED), false);
            }
        }
        if (players.isEmpty()) return;
        Random random = world.getRandom();

        // Blood Moon: a fresh wave around every player every 30 seconds.
        if (bloodMoon && world.getTime() % 600 == 0) {
            for (ServerPlayerEntity player : players) {
                spawnHorde(world, player, 5 + (int) Math.min(10, day / 3), true);
            }
        }

        // Wandering hordes.
        if (cfg.hordesEnabled) {
            if (state.nextHordeTime < 0) state.nextHordeTime = world.getTime() + nextHordeDelay(random);
            if (world.getTime() >= state.nextHordeTime) {
                ServerPlayerEntity target = players.get(random.nextInt(players.size()));
                int size = 6 + (int) Math.min(14, day / 2);
                spawnHorde(world, target, size, false);
                target.sendMessage(Text.translatable("message.deadhorizon.horde").formatted(Formatting.RED), false);
                state.nextHordeTime = world.getTime() + nextHordeDelay(random);
                state.markDirty();
            }
        }

        // Supply drop around noon every few days.
        if (cfg.supplyDropEveryDays > 0 && day % cfg.supplyDropEveryDays == 0 && state.lastDropDay != day
                && world.getTimeOfDay() % 24000L >= 6000L && world.getTimeOfDay() % 24000L < 12000L) {
            state.lastDropDay = day;
            ServerPlayerEntity near = players.get(random.nextInt(players.size()));
            BlockPos drop = supplyDrop(world, near, random);
            if (drop != null) {
                state.lastDrop = drop;
                world.getServer().getPlayerManager().broadcast(Text.translatable("message.deadhorizon.supply_drop",
                        drop.getX(), drop.getZ()).formatted(Formatting.GOLD), false);
            }
            state.markDirty();
        }
    }

    private static long nextHordeDelay(Random random) {
        DHConfig cfg = DHConfig.INSTANCE;
        int min = Math.max(1, cfg.hordeMinMinutes);
        int max = Math.max(min, cfg.hordeMaxMinutes);
        return (long) (min + random.nextInt(max - min + 1)) * 1200L;
    }

    /** Spawns a group 30-50 blocks from the player that heads straight for them. */
    public static void spawnHorde(ServerWorld world, ServerPlayerEntity player, int size, boolean enraged) {
        Random random = world.getRandom();
        double angle = random.nextDouble() * Math.PI * 2;
        double distance = 30 + random.nextDouble() * 20;
        int cx = MathHelper.floor(player.getX() + Math.cos(angle) * distance);
        int cz = MathHelper.floor(player.getZ() + Math.sin(angle) * distance);
        if (!world.isChunkLoaded(cx >> 4, cz >> 4)) return;
        for (int i = 0; i < size; i++) {
            int x = cx + random.nextInt(7) - 3;
            int z = cz + random.nextInt(7) - 3;
            BlockPos pos = new BlockPos(x, world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z), z);
            if (!world.getFluidState(pos.down()).isEmpty()) continue;
            EntityType<? extends DHZombieEntity> type = pickType(random);
            DHZombieEntity zombie = type.create(world);
            if (zombie == null) continue;
            zombie.refreshPositionAndAngles(pos, random.nextFloat() * 360.0f, 0.0f);
            zombie.initialize(world, world.getLocalDifficulty(pos), SpawnReason.EVENT, null, null);
            zombie.hearNoise(player.getBlockPos());
            if (enraged) {
                zombie.setTarget(player);
                zombie.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 20 * 60 * 6, 0));
                zombie.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 20 * 60 * 6, 0));
            }
            world.spawnEntity(zombie);
        }
        world.playSound(null, BlockPos.ofFloored(cx, player.getY(), cz), SoundEvents.ENTITY_ZOMBIE_AMBIENT,
                SoundCategory.HOSTILE, 4.0f, 0.5f);
    }

    private static EntityType<? extends DHZombieEntity> pickType(Random random) {
        int roll = random.nextInt(100);
        if (roll < 55) return ModEntities.WALKER;
        if (roll < 75) return ModEntities.RUNNER;
        if (roll < 83) return ModEntities.CRAWLER;
        if (roll < 89) return ModEntities.BRUTE;
        if (roll < 93) return ModEntities.SCREAMER;
        if (roll < 96) return ModEntities.SPITTER;
        if (roll < 98) return ModEntities.BLOATER;
        return ModEntities.SOLDIER;
    }

    /** A crate of supplies with a smoking signal fire, 80-150 blocks from the player. */
    private static BlockPos supplyDrop(ServerWorld world, ServerPlayerEntity player, Random random) {
        for (int attempt = 0; attempt < 6; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double distance = 80 + random.nextDouble() * 70;
            int x = MathHelper.floor(player.getX() + Math.cos(angle) * distance);
            int z = MathHelper.floor(player.getZ() + Math.sin(angle) * distance);
            BlockPos pos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
            if (!world.getFluidState(pos.down()).isEmpty() || !world.getFluidState(pos).isEmpty()) continue;
            world.setBlockState(pos, ModBlocks.CRATE.getDefaultState());
            if (world.getBlockEntity(pos) instanceof StorageBlockEntity crate) {
                crate.setLootTable(SUPPLY_LOOT, random.nextLong());
            }
            BlockPos fire = pos.east();
            world.setBlockState(fire, Blocks.HAY_BLOCK.getDefaultState());
            world.setBlockState(fire.up(), Blocks.CAMPFIRE.getDefaultState().with(CampfireBlock.SIGNAL_FIRE, true));
            NoiseSystem.emit(world, pos.toCenterPos(), 48.0);
            return pos;
        }
        return null;
    }

    private static void syncBloodMoon(ServerPlayerEntity player, boolean active) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(active);
        ServerPlayNetworking.send(player, BLOOD_MOON_SYNC, buf);
    }

    /** What the radio reports: day, Blood Moon countdown and the last supply drop. */
    public static Text status(MinecraftServer server) {
        ServerWorld overworld = server.getOverworld();
        long day = day(overworld);
        int every = DHConfig.INSTANCE.bloodMoonEveryDays;
        long untilBloodMoon = every > 0 ? (every - day % every) % every : -1;
        ApocalypseState state = ApocalypseState.get(overworld);
        Text drop = state.lastDrop == null ? Text.translatable("radio.deadhorizon.no_drop")
                : Text.translatable("radio.deadhorizon.drop", state.lastDrop.getX(), state.lastDrop.getZ());
        Text moon = untilBloodMoon < 0 ? Text.empty() : untilBloodMoon == 0
                ? Text.translatable("radio.deadhorizon.blood_moon_today")
                : Text.translatable("radio.deadhorizon.blood_moon_in", untilBloodMoon);
        return Text.translatable("radio.deadhorizon.report", day, moon, drop);
    }
}
