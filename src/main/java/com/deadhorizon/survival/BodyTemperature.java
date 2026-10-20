package com.deadhorizon.survival;

import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Works out the temperature the player's surroundings push them towards (0 freezing .. 100 scorching,
 * 50 comfortable): biome, altitude, time of day, weather, being wet, nearby fires and clothing.
 */
public final class BodyTemperature {
    private BodyTemperature() {
    }

    public static float target(ServerPlayerEntity player, float wetness) {
        ServerWorld world = player.getServerWorld();
        BlockPos pos = player.getBlockPos();
        double t;
        if (world.getRegistryKey() == World.NETHER) {
            t = 85.0;
        } else if (world.getRegistryKey() == World.END) {
            t = 30.0;
        } else {
            float biome = world.getBiome(pos).value().getTemperature();
            // plains (0.8) -> 50, desert (2.0) -> 80, snowy plains (0.0) -> 30
            t = 30.0 + biome * 25.0;
            if (pos.getY() > 100) t -= (pos.getY() - 100) * 0.15;
            boolean outside = world.isSkyVisible(pos.up());
            if (outside) {
                if (world.isNight()) {
                    t -= biome > 1.5f ? 18.0 : 8.0; // deserts get cold at night
                } else if (biome > 1.0f) {
                    t += 5.0; // midday sun
                }
                if (world.hasRain(pos.up())) t -= 8.0;
                else if (world.isRaining() && biome < 0.15f) t -= 12.0; // snowfall
            } else if (pos.getY() < 50) {
                t = t * 0.5 + 22.0; // deep underground stays mild
            }
        }

        if (player.isSubmergedInWater()) t -= 15.0;
        else if (player.isTouchingWater()) t -= 10.0;
        t -= wetness * 0.15; // wet clothes
        if (player.isOnFire()) t += 25.0;

        t += Math.min(30.0, heatNearby(world, pos));

        int clothing = clothing(player);
        if (t < 50.0) {
            t = Math.min(50.0, t + clothing * 5.0); // warm clothes keep the cold out
        } else if (t > 60.0) {
            t += clothing * 2.0; // and make the heat worse
        }
        return (float) Math.max(0.0, Math.min(100.0, t));
    }

    /** Warmth from fires, lava, lit furnaces and the like within a few blocks. */
    private static double heatNearby(ServerWorld world, BlockPos center) {
        double heat = 0.0;
        for (BlockPos pos : BlockPos.iterate(center.add(-3, -2, -3), center.add(3, 2, 3))) {
            BlockState state = world.getBlockState(pos);
            if (state.isOf(Blocks.LAVA) || state.isOf(Blocks.FIRE) || state.isOf(Blocks.SOUL_FIRE)) {
                heat += 12.0;
            } else if (state.isOf(Blocks.MAGMA_BLOCK)) {
                heat += 5.0;
            } else if (state.getBlock() instanceof CampfireBlock && state.get(CampfireBlock.LIT)) {
                heat += 15.0;
            } else if (state.getBlock() instanceof AbstractFurnaceBlock && state.get(AbstractFurnaceBlock.LIT)) {
                heat += 8.0;
            }
        }
        return heat;
    }

    /** Leather pieces worn: the only proper warm clothing so far. */
    private static int clothing(ServerPlayerEntity player) {
        int pieces = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getEquippedStack(slot);
            if (stack.getItem() instanceof ArmorItem armor && armor.getMaterial() == ArmorMaterials.LEATHER) pieces++;
        }
        return pieces;
    }
}
