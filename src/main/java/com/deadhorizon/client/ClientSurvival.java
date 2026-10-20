package com.deadhorizon.client;

import com.deadhorizon.registry.ModEffects;
import com.deadhorizon.survival.Survival;
import com.deadhorizon.survival.SurvivalData;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;

/** The client's copy of the server's thirst and stamina, for the HUD and sprint blocking. */
public final class ClientSurvival {
    public static float thirst = SurvivalData.MAX_THIRST;
    public static float stamina = SurvivalData.MAX_STAMINA;
    public static boolean exhausted;

    private ClientSurvival() {
    }

    public static boolean blocksSprint(ClientPlayerEntity player) {
        return !player.isCreative() && (exhausted || player.hasStatusEffect(ModEffects.FRACTURE));
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(Survival.SYNC, (client, handler, buf, responseSender) -> {
            float newThirst = buf.readFloat();
            float newStamina = buf.readFloat();
            boolean newExhausted = buf.readBoolean();
            client.execute(() -> {
                thirst = newThirst;
                stamina = newStamina;
                exhausted = newExhausted;
            });
        });
        // Backup in case the sprint check mixin could not apply.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayerEntity player = client.player;
            if (player != null && player.isSprinting() && blocksSprint(player)) player.setSprinting(false);
        });
    }
}
