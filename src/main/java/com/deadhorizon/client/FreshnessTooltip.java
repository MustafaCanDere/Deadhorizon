package com.deadhorizon.client;

import com.deadhorizon.food.Spoilage;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Shows how fresh perishable food is. */
public final class FreshnessTooltip {
    private FreshnessTooltip() {
    }

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null) return;
            float age = Spoilage.age(stack, client.world.getTime());
            if (age < 0) return;
            if (age < 0.5f) {
                lines.add(Text.translatable("tooltip.deadhorizon.fresh").formatted(Formatting.GREEN));
            } else if (age < 0.8f) {
                lines.add(Text.translatable("tooltip.deadhorizon.stale").formatted(Formatting.YELLOW));
            } else {
                lines.add(Text.translatable("tooltip.deadhorizon.rotting").formatted(Formatting.RED));
            }
        });
    }
}
