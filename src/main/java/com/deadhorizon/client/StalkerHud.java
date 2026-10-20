package com.deadhorizon.client;

import com.deadhorizon.config.DHConfig;
import com.deadhorizon.registry.ModEffects;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

/**
 * STALKER / Dying Light style overlay: a compass strip at the top, status badges and condition bars in the
 * bottom-right corner, and a red marker around the crosshair pointing at whatever just hurt you.
 */
public final class StalkerHud {
    private static final int YELLOW = 0xFFE0C040, ORANGE = 0xFFE07020, RED = 0xFFD02020;

    private StalkerHud() {
    }

    public static void register() {
        HudRenderCallback.EVENT.register(StalkerHud::render);
    }

    private static void render(DrawContext ctx, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.options.hudHidden || client.interactionManager == null
                || !client.interactionManager.hasStatusBars()) return;
        int w = ctx.getScaledWindowWidth();
        int h = ctx.getScaledWindowHeight();
        drawCompass(ctx, client.textRenderer, player, w, tickDelta);
        drawBars(ctx, client.textRenderer, player, w, h);
        drawBadges(ctx, client.textRenderer, player, w, h);
        drawHitMarker(ctx, player, w, h);
    }

    private static void drawCompass(DrawContext ctx, TextRenderer text, ClientPlayerEntity player, int w, float tickDelta) {
        int width = 160, x0 = w / 2 - width / 2, y = 4;
        ctx.fill(x0, y, x0 + width, y + 11, 0x66000000);
        float yaw = MathHelper.wrapDegrees(player.getYaw(tickDelta));
        String[] names = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
        for (int i = 0; i < 8; i++) {
            float rel = MathHelper.wrapDegrees(i * 45.0f - yaw);
            if (Math.abs(rel) > 80) continue;
            int x = w / 2 + Math.round(rel / 80.0f * (width / 2.0f));
            boolean major = i % 2 == 0;
            int color = names[i].equals("N") ? 0xFFFF5050 : major ? 0xFFFFFFFF : 0xFFA0A0A0;
            ctx.drawCenteredTextWithShadow(text, names[i], x, y + 2, color);
        }
        for (int deg = 0; deg < 360; deg += 15) {
            float rel = MathHelper.wrapDegrees(deg - yaw);
            if (Math.abs(rel) > 80 || deg % 45 == 0) continue;
            int x = w / 2 + Math.round(rel / 80.0f * (width / 2.0f));
            ctx.fill(x, y + 8, x + 1, y + 10, 0x80FFFFFF);
        }
        ctx.fill(w / 2, y + 11, w / 2 + 1, y + 14, 0xFFFFFFFF);
    }

    private static void bar(DrawContext ctx, TextRenderer text, String label, int x, int y, float fraction, int color) {
        int width = 60;
        ctx.drawTextWithShadow(text, label, x - text.getWidth(label) - 3, y - 1, 0xFFC8C8C8);
        ctx.fill(x - 1, y - 1, x + width + 1, y + 6, 0xA0000000);
        ctx.fill(x, y, x + Math.round(width * MathHelper.clamp(fraction, 0, 1)), y + 5, color);
    }

    private static void drawBars(DrawContext ctx, TextRenderer text, ClientPlayerEntity player, int w, int h) {
        int x = w - 66, y = h - 12;
        bar(ctx, text, Text.translatable("hud.deadhorizon.bar.health").getString(), x, y,
                player.getHealth() / player.getMaxHealth(), 0xFFC03030);
        y -= 9;
        if (DHConfig.INSTANCE.staminaEnabled) {
            bar(ctx, text, Text.translatable("hud.deadhorizon.bar.stamina").getString(), x, y,
                    ClientSurvival.stamina / 100.0f, ClientSurvival.exhausted ? 0xFF806020 : 0xFF60B040);
            y -= 9;
        }
        if (ClientSurvival.radiation > 0.5f) {
            bar(ctx, text, Text.translatable("hud.deadhorizon.bar.radiation").getString(), x, y,
                    ClientSurvival.radiation / 100.0f, 0xFFE0D020);
        }
    }

    private static void drawBadges(DrawContext ctx, TextRenderer text, ClientPlayerEntity player, int w, int h) {
        int x = w - 6;
        int y = h - 50;
        y = badge(ctx, text, x, y, "rad", ClientSurvival.radiation, 10, 30, 60);
        y = badge(ctx, text, x, y, "bleeding", effect(player, ModEffects.BLEEDING), 1, 2, 3);
        y = badge(ctx, text, x, y, "infection", effect(player, ModEffects.INFECTION), 1, 2, 3);
        y = badge(ctx, text, x, y, "thirst", 20 - ClientSurvival.thirst, 12, 15, 18);
        y = badge(ctx, text, x, y, "hunger", 20 - player.getHungerManager().getFoodLevel(), 12, 15, 18);
        float t = ClientSurvival.temperature;
        y = badge(ctx, text, x, y, t < 50 ? "cold" : "hot", Math.abs(t - 50), 20, 28, 40);
        y = badge(ctx, text, x, y, "fracture", effect(player, ModEffects.FRACTURE) + effect(player, ModEffects.BROKEN_ARM), 1, 1, 2);
        badge(ctx, text, x, y, "tired", 100 - ClientSurvival.rest, 70, 80, 90);
    }

    private static float effect(ClientPlayerEntity player, StatusEffect effect) {
        var instance = player.getStatusEffect(effect);
        return instance == null ? 0 : instance.getAmplifier() + 1;
    }

    /** One STALKER-style icon: yellow, orange or red by how bad it is; nothing when it's fine. */
    private static int badge(DrawContext ctx, TextRenderer text, int right, int y, String key, float value,
                             float warn, float bad, float critical) {
        if (value < warn) return y;
        int color = value >= critical ? RED : value >= bad ? ORANGE : YELLOW;
        String label = Text.translatable("hud.deadhorizon.badge." + key).getString();
        int width = text.getWidth(label) + 6;
        ctx.fill(right - width, y, right, y + 11, 0xB0000000);
        ctx.fill(right - width, y, right - width + 2, y + 11, color);
        ctx.drawTextWithShadow(text, label, right - width + 4, y + 2, color);
        return y - 13;
    }

    private static void drawHitMarker(DrawContext ctx, ClientPlayerEntity player, int w, int h) {
        if (player.hurtTime <= 0) return;
        float rel = (float) Math.toRadians(player.getDamageTiltYaw() - 90.0f);
        int cx = w / 2, cy = h / 2;
        int r = 36;
        int alpha = (int) (255 * player.hurtTime / 10.0f) << 24;
        for (int i = -3; i <= 3; i++) {
            double a = rel + i * 0.06;
            int x = cx + (int) Math.round(Math.sin(a) * r);
            int y = cy - (int) Math.round(Math.cos(a) * r);
            ctx.fill(x - 1, y - 1, x + 2, y + 2, alpha | 0xD02020);
        }
    }
}
