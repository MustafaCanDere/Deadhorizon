package com.deadhorizon.effect;

import com.deadhorizon.config.DHConfig;
import com.deadhorizon.entity.zombie.DHZombieEntity;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;

/** Blood spatters from hits: bright red for the living, dark for the dead. */
public final class BloodEffects {
    private static final BlockStateParticleEffect FRESH =
            new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.getDefaultState());
    private static final BlockStateParticleEffect ROTTEN =
            new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.NETHER_WART_BLOCK.getDefaultState());

    private BloodEffects() {
    }

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (DHConfig.INSTANCE.bloodEffects && amount >= 1.0f && entity.getWorld() instanceof ServerWorld world
                    && !entity.isBlocking() && bleeds(entity)) {
                int count = Math.min(24, (int) (amount * 3));
                world.spawnParticles(entity instanceof DHZombieEntity ? ROTTEN : FRESH,
                        entity.getX(), entity.getBodyY(0.6), entity.getZ(), count, 0.2, 0.25, 0.2, 0.1);
            }
            return true;
        });
    }

    private static boolean bleeds(LivingEntity entity) {
        return entity instanceof PlayerEntity || entity instanceof DHZombieEntity || entity instanceof AnimalEntity;
    }
}
