package com.deadhorizon.client;

import com.deadhorizon.config.DHConfig;
import com.deadhorizon.registry.ModEffects;
import com.deadhorizon.survival.Survival;
import com.deadhorizon.survival.SurvivalData;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

/** The client's copy of the server's survival stats, for the HUD and sprint blocking. */
public final class ClientSurvival {
    public static float thirst = SurvivalData.MAX_THIRST;
    public static float stamina = SurvivalData.MAX_STAMINA;
    public static boolean exhausted;
    public static float temperature = SurvivalData.NORMAL_TEMPERATURE;
    public static float rest = SurvivalData.MAX_REST;
    public static int load;
    public static float wetness;

    private ClientSurvival() {
    }

    public static boolean blocksSprint(ClientPlayerEntity player) {
        return !player.isCreative() && (exhausted || player.hasStatusEffect(ModEffects.FRACTURE));
    }

    private static int beatTimer;

    /** Hear your own heart pounding at low health, faster the closer you are to death. */
    private static void heartbeat(MinecraftClient client, ClientPlayerEntity player) {
        if (!DHConfig.INSTANCE.bloodEffects || player.isCreative() || !player.isAlive() || player.getHealth() > 6.0f) {
            beatTimer = 0;
            return;
        }
        int interval = 14 + Math.round(player.getHealth() * 3);
        if (beatTimer == 0 || beatTimer == 6) {
            float pitch = beatTimer == 0 ? 0.55f : 0.5f;
            client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM.value(), pitch, 0.7f));
        }
        beatTimer = (beatTimer + 1) % interval;
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(Survival.SYNC, (client, handler, buf, responseSender) -> {
            float newThirst = buf.readFloat();
            float newStamina = buf.readFloat();
            boolean newExhausted = buf.readBoolean();
            float newTemperature = buf.readFloat();
            float newRest = buf.readFloat();
            int newLoad = buf.readByte();
            float newWetness = buf.readFloat();
            client.execute(() -> {
                thirst = newThirst;
                stamina = newStamina;
                exhausted = newExhausted;
                temperature = newTemperature;
                rest = newRest;
                load = newLoad;
                wetness = newWetness;
            });
        });
        // Backup in case the sprint check mixin could not apply.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayerEntity player = client.player;
            if (player != null && player.isSprinting() && blocksSprint(player)) player.setSprinting(false);
            if (player != null) heartbeat(client, player);
        });
    }
}
