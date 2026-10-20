package com.deadhorizon.client;

import com.deadhorizon.DeadHorizon;
import com.deadhorizon.config.DHConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/**
 * Water drops above the hunger bar, a small stamina bar under the crosshair when not full, and to the left
 * of the hotbar a thermometer with short warnings for tiredness and a heavy pack.
 */
public final class SurvivalHud {
    private static final Identifier THIRST = DeadHorizon.id("textures/gui/thirst.png");

    private SurvivalHud() {
    }

    public static void register() {
        HudRenderCallback.EVENT.register(SurvivalHud::render);
    }

    private static void render(DrawContext ctx, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.options.hudHidden || client.interactionManager == null
                || !client.interactionManager.hasStatusBars()) return;
        int width = ctx.getScaledWindowWidth();
        int height = ctx.getScaledWindowHeight();
        if (DHConfig.INSTANCE.thirstEnabled) drawThirst(ctx, player, width, height);
        if (DHConfig.INSTANCE.staminaEnabled) drawStamina(ctx, width, height);
        drawStatus(ctx, client, width, height);
    }

    private static void drawThirst(DrawContext ctx, ClientPlayerEntity player, int width, int height) {
        int y = height - 49;
        if (player.isSubmergedIn(FluidTags.WATER) || player.getAir() < player.getMaxAir()) y -= 10;
        if (player.getVehicle() instanceof LivingEntity) y -= 10;
        int right = width / 2 + 91;
        int value = Math.round(ClientSurvival.thirst);
        // Shake the drops when nearly dry, like the hunger bar does.
        boolean shake = value <= 4 && player.age % (value * 3 + 1) == 0;
        for (int i = 0; i < 10; i++) {
            int x = right - i * 8 - 9;
            int dy = shake ? player.getRandom().nextInt(3) - 1 : 0;
            ctx.drawTexture(THIRST, x, y + dy, 0, 0, 9, 9, 27, 9);
            if (i * 2 + 1 < value) {
                ctx.drawTexture(THIRST, x, y + dy, 9, 0, 9, 9, 27, 9);
            } else if (i * 2 + 1 == value) {
                ctx.drawTexture(THIRST, x, y + dy, 18, 0, 9, 9, 27, 9);
            }
        }
    }

    private static void drawStamina(DrawContext ctx, int width, int height) {
        float stamina = ClientSurvival.stamina;
        if (stamina >= 99.5f && !ClientSurvival.exhausted) return;
        int barWidth = 40;
        int x = width / 2 - barWidth / 2;
        int y = height / 2 + 10;
        int filled = Math.round(barWidth * stamina / 100.0f);
        int color = ClientSurvival.exhausted ? 0xFFD03A2F : stamina < 30.0f ? 0xFFE0A030 : 0xFFE8E0C8;
        ctx.fill(x - 1, y - 1, x + barWidth + 1, y + 3, 0x80000000);
        ctx.fill(x, y, x + filled, y + 2, color);
    }

    private static void drawStatus(DrawContext ctx, MinecraftClient client, int width, int height) {
        // Left of the off-hand slot, so the two never overlap.
        int x = width / 2 - 91 - 40;
        int bottom = height - 3;
        if (DHConfig.INSTANCE.temperatureEnabled) {
            float t = ClientSurvival.temperature;
            int top = bottom - 18;
            ctx.fill(x - 1, top - 1, x + 5, bottom + 1, 0xA0000000);
            int filled = Math.round(18 * t / 100.0f);
            ctx.fill(x, bottom - filled, x + 4, bottom, temperatureColor(t));
            // Marks for "too cold" and "too hot".
            ctx.fill(x + 4, bottom - Math.round(18 * 0.25f), x + 6, bottom - Math.round(18 * 0.25f) + 1, 0xFF7FC8FF);
            ctx.fill(x + 4, bottom - Math.round(18 * 0.75f), x + 6, bottom - Math.round(18 * 0.75f) + 1, 0xFFFF8040);
            bottom = top - 4;
        }
        if (DHConfig.INSTANCE.fatigueEnabled && ClientSurvival.rest < 30.0f) {
            Formatting color = ClientSurvival.rest < 10.0f ? Formatting.RED : Formatting.GRAY;
            Text text = Text.translatable("hud.deadhorizon.tired").formatted(color);
            ctx.drawTextWithShadow(client.textRenderer, text, x + 5 - client.textRenderer.getWidth(text), bottom - 8, 0xFFFFFF);
            bottom -= 10;
        }
        if (DHConfig.INSTANCE.wetnessEnabled && ClientSurvival.wetness >= 20.0f) {
            Formatting color = ClientSurvival.wetness >= 70.0f ? Formatting.BLUE : Formatting.AQUA;
            Text text = Text.translatable(ClientSurvival.wetness >= 70.0f ? "hud.deadhorizon.soaked" : "hud.deadhorizon.wet").formatted(color);
            ctx.drawTextWithShadow(client.textRenderer, text, x + 5 - client.textRenderer.getWidth(text), bottom - 8, 0xFFFFFF);
            bottom -= 10;
        }
        if (DHConfig.INSTANCE.encumbranceEnabled && ClientSurvival.load > 0) {
            Formatting color = ClientSurvival.load == 2 ? Formatting.RED : Formatting.YELLOW;
            Text text = Text.translatable(ClientSurvival.load == 2 ? "hud.deadhorizon.overloaded" : "hud.deadhorizon.heavy").formatted(color);
            ctx.drawTextWithShadow(client.textRenderer, text, x + 5 - client.textRenderer.getWidth(text), bottom - 8, 0xFFFFFF);
        }
    }

    private static int temperatureColor(float t) {
        if (t < 12.0f) return 0xFFB0E8FF;
        if (t < 25.0f) return 0xFF4FA3E0;
        if (t <= 75.0f) return 0xFF6CC24A;
        if (t <= 90.0f) return 0xFFE08A30;
        return 0xFFD03A2F;
    }
}
