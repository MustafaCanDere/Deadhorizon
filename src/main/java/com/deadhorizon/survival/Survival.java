package com.deadhorizon.survival;

import com.deadhorizon.DeadHorizon;
import com.deadhorizon.config.DHConfig;
import com.deadhorizon.registry.ModDamageTypes;
import com.deadhorizon.registry.ModEffects;
import com.deadhorizon.registry.ModItems;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

/** Thirst, stamina and broken legs. Server-authoritative; the client only gets a copy for the HUD. */
public final class Survival {
    public static final Identifier SYNC = DeadHorizon.id("survival_sync");

    private static final float EXHAUSTION_RECOVERY = 30.0f;
    private static final Map<Item, Float> FOOD_THIRST = new HashMap<>();
    private static final Map<Item, Float> FOOD_STAMINA = new HashMap<>();

    private Survival() {
    }

    public static SurvivalData get(PlayerEntity player) {
        return ((SurvivalHolder) player).deadhorizon$getSurvival();
    }

    public static void register() {
        FOOD_THIRST.put(Items.MELON_SLICE, 3.0f);
        FOOD_THIRST.put(Items.SWEET_BERRIES, 1.0f);
        FOOD_THIRST.put(Items.GLOW_BERRIES, 1.0f);
        FOOD_THIRST.put(Items.APPLE, 1.5f);
        FOOD_THIRST.put(Items.CARROT, 0.5f);
        FOOD_THIRST.put(Items.MUSHROOM_STEW, 4.0f);
        FOOD_THIRST.put(Items.BEETROOT_SOUP, 4.0f);
        FOOD_THIRST.put(Items.RABBIT_STEW, 4.0f);
        FOOD_THIRST.put(Items.SUSPICIOUS_STEW, 4.0f);
        FOOD_THIRST.put(Items.HONEY_BOTTLE, 2.0f);
        FOOD_THIRST.put(Items.BREAD, -0.5f);
        FOOD_THIRST.put(Items.COOKIE, -0.5f);
        FOOD_THIRST.put(Items.DRIED_KELP, -1.0f);
        FOOD_THIRST.put(Items.COOKED_COD, -0.5f);
        FOOD_THIRST.put(Items.COOKED_SALMON, -0.5f);
        FOOD_THIRST.put(Items.ROTTEN_FLESH, -1.0f);
        FOOD_THIRST.put(ModItems.CANNED_SOUP, 5.0f);
        FOOD_THIRST.put(ModItems.CANNED_BEANS, 1.0f);
        FOOD_THIRST.put(ModItems.MRE, 2.0f);
        FOOD_THIRST.put(ModItems.ENERGY_BAR, -0.5f);
        FOOD_STAMINA.put(ModItems.ENERGY_BAR, 35.0f);
        FOOD_STAMINA.put(ModItems.MRE, 20.0f);
        FOOD_STAMINA.put(Items.HONEY_BOTTLE, 10.0f);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tick(player);
            }
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient && DHConfig.INSTANCE.staminaEnabled && !exempt(player)) {
                exert(player, (float) DHConfig.INSTANCE.attackStaminaCost);
            }
            return ActionResult.PASS;
        });

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (DHConfig.INSTANCE.fracturesEnabled && entity instanceof ServerPlayerEntity player
                    && source.isIn(DamageTypeTags.IS_FALL) && amount >= 5.0f && !player.hasStatusEffect(ModEffects.FRACTURE)) {
                float chance = Math.min(0.9f, (amount - 4.0f) * 0.15f);
                if (player.getRandom().nextFloat() < chance) {
                    player.addStatusEffect(new StatusEffectInstance(ModEffects.FRACTURE,
                            DHConfig.INSTANCE.fractureMinutes * 60 * 20, 0, false, false, true));
                    player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ITEM_BREAK,
                            SoundCategory.PLAYERS, 1.0f, 0.5f);
                    message(player, "message.deadhorizon.fracture", Formatting.RED);
                }
            }
            return true;
        });

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (alive) get(newPlayer).copyFrom(get(oldPlayer));
        });
    }

    private static boolean exempt(PlayerEntity player) {
        return player.isCreative() || player.isSpectator();
    }

    private static void tick(ServerPlayerEntity player) {
        SurvivalData data = get(player);
        DHConfig cfg = DHConfig.INSTANCE;
        if (!exempt(player) && player.isAlive()) {
            if (cfg.thirstEnabled) tickThirst(player, data, cfg);
            if (cfg.staminaEnabled) tickStamina(player, data, cfg);
            if (player.hasStatusEffect(ModEffects.FRACTURE) && player.isSprinting()) player.setSprinting(false);
        }
        if (player.age % 5 == 0) sync(player, data);
    }

    private static void tickThirst(ServerPlayerEntity player, SurvivalData data, DHConfig cfg) {
        ServerWorld world = player.getServerWorld();
        double rate = cfg.thirstDrainPerTick;
        if (player.isSprinting()) rate *= 3.0;
        if (world.getRegistryKey() == World.NETHER) {
            rate *= 2.0;
        } else if (world.getBiome(player.getBlockPos()).value().getTemperature() > 1.0f) {
            rate *= 1.5;
        }
        if (player.hasStatusEffect(StatusEffects.HUNGER)) rate *= 1.5;
        data.addThirst((float) -rate);

        if (player.age % 40 == 0) {
            if (data.thirst < 6.0f) refresh(player, StatusEffects.WEAKNESS, 0);
            if (data.thirst < 3.0f) refresh(player, StatusEffects.SLOWNESS, 0);
        }
        if (data.thirst <= 0.0f && player.age % 80 == 0) {
            player.damage(ModDamageTypes.of(player.getWorld(), ModDamageTypes.DEHYDRATION), 1.0f);
        }
    }

    private static void tickStamina(ServerPlayerEntity player, SurvivalData data, DHConfig cfg) {
        if (player.isSprinting() && !player.hasVehicle()) {
            // Heavy armour makes running harder: full diamond (20 armour) is +50%.
            exert(player, (float) (cfg.sprintStaminaCost * (1.0 + player.getArmor() / 40.0)));
        } else if (data.regenDelay > 0) {
            data.regenDelay--;
        } else {
            double regen = cfg.staminaRegenPerTick;
            if (data.thirst < 6.0f) regen *= 0.5;
            if (player.hasStatusEffect(ModEffects.FRACTURE)) regen *= 0.5;
            if (player.isSneaking() || player.getVelocity().horizontalLengthSquared() < 1.0E-4) regen *= 1.5;
            data.addStamina((float) regen);
        }

        if (data.exhausted) {
            if (data.stamina >= EXHAUSTION_RECOVERY) {
                data.exhausted = false;
            } else {
                if (player.isSprinting()) player.setSprinting(false);
                if (player.age % 20 == 0) {
                    refresh(player, StatusEffects.WEAKNESS, 0);
                    refresh(player, StatusEffects.MINING_FATIGUE, 0);
                }
            }
        }
    }

    /** Spends stamina; running dry leaves the player winded until they recover. */
    public static void exert(PlayerEntity player, float cost) {
        SurvivalData data = get(player);
        data.addStamina(-cost);
        data.regenDelay = 20;
        if (data.stamina <= 0.0f && !data.exhausted) {
            data.exhausted = true;
            player.setSprinting(false);
            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_HORSE_BREATHE,
                    SoundCategory.PLAYERS, 1.0f, 1.5f);
            message(player, "message.deadhorizon.exhausted", Formatting.YELLOW);
        }
    }

    /** Called for anything eaten through PlayerEntity#eatFood. */
    public static void onConsume(PlayerEntity player, ItemStack stack) {
        Float thirst = FOOD_THIRST.get(stack.getItem());
        if (thirst != null) get(player).addThirst(thirst);
        Float stamina = FOOD_STAMINA.get(stack.getItem());
        if (stamina != null) get(player).addStamina(stamina);
    }

    public static void drink(PlayerEntity player, float thirst) {
        get(player).addThirst(thirst);
    }

    /** Unboiled water: quenches a little but may make you sick. */
    public static void drinkDirtyWater(PlayerEntity player) {
        drink(player, 4.0f);
        if (player.getRandom().nextDouble() < DHConfig.INSTANCE.dirtyWaterSicknessChance) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 20 * 12));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 20 * 40));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 20 * 4));
            message(player, "message.deadhorizon.dirty_water", Formatting.DARK_GREEN);
        }
    }

    private static void refresh(PlayerEntity player, StatusEffect effect, int amplifier) {
        StatusEffectInstance current = player.getStatusEffect(effect);
        if (current == null || current.getDuration() < 50) {
            player.addStatusEffect(new StatusEffectInstance(effect, 60, amplifier, false, false, true));
        }
    }

    private static void sync(ServerPlayerEntity player, SurvivalData data) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeFloat(data.thirst);
        buf.writeFloat(data.stamina);
        buf.writeBoolean(data.exhausted);
        ServerPlayNetworking.send(player, SYNC, buf);
    }

    private static void message(PlayerEntity player, String key, Formatting color) {
        player.sendMessage(Text.translatable(key).formatted(color), true);
    }
}
