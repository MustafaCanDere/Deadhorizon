package com.deadhorizon.client;

import com.deadhorizon.config.DHConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

/** The PDA (P): day and time, the next Blood Moon, every body stat with a bar, and active conditions. */
public class PdaScreen extends Screen {
    private static final int W = 240, H = 180;

    public PdaScreen() {
        super(Text.translatable("screen.deadhorizon.pda"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        int x = (width - W) / 2, y = (height - H) / 2;
        ctx.fill(x - 4, y - 4, x + W + 4, y + H + 4, 0xFF2A2C24);        // case
        ctx.fill(x, y, x + W, y + H, 0xFF0E1A10);                         // screen
        ctx.fill(x, y, x + W, y + 14, 0xFF1E3A22);
        ctx.drawTextWithShadow(textRenderer, title, x + 6, y + 3, 0xFF9CFF9C);

        long time = player.getWorld().getTimeOfDay();
        long day = time / 24000L + 1;
        long tod = (time + 6000L) % 24000L;  // 0 = midnight
        String clock = String.format("%02d:%02d", tod / 1000, (tod % 1000) * 60 / 1000);
        ctx.drawTextWithShadow(textRenderer, Text.translatable("screen.deadhorizon.pda.day", day, clock), x + W - 6
                - textRenderer.getWidth(Text.translatable("screen.deadhorizon.pda.day", day, clock)), y + 3, 0xFF9CFF9C);

        int every = DHConfig.INSTANCE.bloodMoonEveryDays;
        int line = y + 20;
        if (every > 0) {
            long left = (every - day % every) % every;
            Text moon = ClientSurvival.bloodMoon || left == 0 ? Text.translatable("screen.deadhorizon.pda.moon_now")
                    : Text.translatable("screen.deadhorizon.pda.moon_in", left);
            ctx.drawTextWithShadow(textRenderer, moon, x + 6, line, left == 0 ? 0xFFFF6060 : 0xFFC8C8C8);
            line += 14;
        }

        line = stat(ctx, x, line, "health", player.getHealth() / player.getMaxHealth(), 0xFFC03030);
        line = stat(ctx, x, line, "stamina", ClientSurvival.stamina / 100.0f, 0xFF60B040);
        line = stat(ctx, x, line, "thirst", ClientSurvival.thirst / 20.0f, 0xFF4090E0);
        line = stat(ctx, x, line, "food", player.getHungerManager().getFoodLevel() / 20.0f, 0xFFC08040);
        line = stat(ctx, x, line, "temperature", ClientSurvival.temperature / 100.0f, 0xFFE07020);
        line = stat(ctx, x, line, "rest", ClientSurvival.rest / 100.0f, 0xFF9080E0);
        line = stat(ctx, x, line, "wetness", ClientSurvival.wetness / 100.0f, 0xFF40C0C0);
        line = stat(ctx, x, line, "radiation", ClientSurvival.radiation / 100.0f, 0xFFE0D020);

        int ex = x + 150, ey = y + 34;
        ctx.drawTextWithShadow(textRenderer, Text.translatable("screen.deadhorizon.pda.conditions"), ex, ey, 0xFF9CFF9C);
        ey += 12;
        if (player.getStatusEffects().isEmpty()) {
            ctx.drawTextWithShadow(textRenderer, Text.translatable("screen.deadhorizon.pda.healthy"), ex, ey, 0xFF808080);
        }
        for (StatusEffectInstance effect : player.getStatusEffects()) {
            if (ey > y + H - 12) break;
            Text name = effect.getEffectType().getName();
            int color = effect.getEffectType().isBeneficial() ? 0xFF80E080 : 0xFFE08080;
            ctx.drawTextWithShadow(textRenderer, name, ex, ey, color);
            ey += 10;
        }
        super.render(ctx, mouseX, mouseY, delta);
    }

    private int stat(DrawContext ctx, int x, int y, String key, float fraction, int color) {
        Text label = Text.translatable("screen.deadhorizon.pda." + key);
        ctx.drawTextWithShadow(textRenderer, label, x + 6, y, 0xFFC8C8C8);
        int bx = x + 70, bw = 70;
        ctx.fill(bx - 1, y, bx + bw + 1, y + 8, 0xFF000000);
        ctx.fill(bx, y + 1, bx + Math.round(bw * MathHelper.clamp(fraction, 0, 1)), y + 7, color);
        return y + 14;
    }
}
