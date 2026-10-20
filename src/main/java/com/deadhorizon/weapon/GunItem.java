package com.deadhorizon.weapon;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A firearm. Right click fires; holding it fires full auto when the gun is in auto mode.
 * Loaded rounds, fire mode, reloads and jams live in the stack's NBT.
 */
public class GunItem extends Item {
    private static final String AMMO = "Ammo";
    private static final String AUTO = "Auto";
    private static final String RELOAD_UNTIL = "ReloadUntil";
    private static final String JAMMED = "Jammed";
    private static final String ATTACHMENTS = "Attachments";

    private final GunSpec spec;

    public GunItem(Settings settings, GunSpec spec) {
        super(settings);
        this.spec = spec;
    }

    public GunSpec spec() {
        return spec;
    }

    public static boolean has(ItemStack stack, AttachmentItem.Type type) {
        NbtCompound nbt = stack.getNbt();
        return nbt != null && nbt.getCompound(ATTACHMENTS).getBoolean(type.id());
    }

    public static void attach(ItemStack stack, AttachmentItem.Type type) {
        NbtCompound attachments = stack.getOrCreateNbt().getCompound(ATTACHMENTS);
        attachments.putBoolean(type.id(), true);
        stack.getOrCreateNbt().put(ATTACHMENTS, attachments);
    }

    /** Magazine size, counting an extended magazine. */
    public int magazine(ItemStack stack) {
        return has(stack, AttachmentItem.Type.EXTENDED_MAG) ? spec.magazine() * 3 / 2 : spec.magazine();
    }

    /** How far zombies hear it, counting a suppressor. */
    public double noise(ItemStack stack) {
        return has(stack, AttachmentItem.Type.SUPPRESSOR) ? spec.noise() * 0.25 : spec.noise();
    }

    /** FOV multiplier while aiming, counting a scope. */
    public float zoom(ItemStack stack) {
        return has(stack, AttachmentItem.Type.SCOPE) ? Math.min(spec.zoom(), 0.35f) : spec.zoom();
    }

    public static int ammo(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt == null ? 0 : nbt.getInt(AMMO);
    }

    public static void setAmmo(ItemStack stack, int rounds) {
        stack.getOrCreateNbt().putInt(AMMO, rounds);
    }

    public boolean isAuto(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return spec.automatic() && (nbt == null || !nbt.contains(AUTO) || nbt.getBoolean(AUTO));
    }

    public static void setAuto(ItemStack stack, boolean auto) {
        stack.getOrCreateNbt().putBoolean(AUTO, auto);
    }

    public static boolean isJammed(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt != null && nbt.getBoolean(JAMMED);
    }

    public static void setJammed(ItemStack stack, boolean jammed) {
        stack.getOrCreateNbt().putBoolean(JAMMED, jammed);
    }

    public static long reloadUntil(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        return nbt == null ? 0L : nbt.getLong(RELOAD_UNTIL);
    }

    public static void setReloadUntil(ItemStack stack, long time) {
        if (time <= 0) {
            NbtCompound nbt = stack.getNbt();
            if (nbt != null) nbt.remove(RELOAD_UNTIL);
        } else {
            stack.getOrCreateNbt().putLong(RELOAD_UNTIL, time);
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (hand != Hand.MAIN_HAND) return TypedActionResult.pass(stack);
        if (isAuto(stack)) {
            user.setCurrentHand(hand);
            if (!world.isClient) Guns.tryFire((ServerPlayerEntity) user, stack, this);
            return TypedActionResult.consume(stack);
        }
        if (!world.isClient) Guns.tryFire((ServerPlayerEntity) user, stack, this);
        user.getItemCooldownManager().set(this, Math.max(spec.interval(), 4));
        return TypedActionResult.success(stack, false);
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        int held = getMaxUseTime(stack) - remainingUseTicks;
        if (!world.isClient && held > 0 && held % spec.interval() == 0 && user instanceof ServerPlayerEntity player) {
            Guns.tryFire(player, stack, this);
        }
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.NONE;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        long until = reloadUntil(stack);
        if (world.isClient || until <= 0 || !(entity instanceof ServerPlayerEntity player)) return;
        if (!selected) {
            setReloadUntil(stack, 0); // put away mid-reload
        } else if (world.getTime() >= until) {
            setReloadUntil(stack, 0);
            Guns.finishReload(player, stack, this);
        }
    }

    @Override
    public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        return false; // don't bob the gun every time a round is fired
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.deadhorizon.gun.ammo", ammo(stack), magazine(stack),
                Text.translatable(spec.ammo().get().getTranslationKey())).formatted(Formatting.GRAY));
        if (spec.automatic()) {
            tooltip.add(Text.translatable(isAuto(stack) ? "tooltip.deadhorizon.gun.auto" : "tooltip.deadhorizon.gun.semi")
                    .formatted(Formatting.DARK_GRAY));
        }
        for (AttachmentItem.Type type : AttachmentItem.Type.values()) {
            if (has(stack, type)) {
                tooltip.add(Text.literal("+ ").append(Text.translatable("item.deadhorizon." + type.id())).formatted(Formatting.BLUE));
            }
        }
        if (isJammed(stack)) tooltip.add(Text.translatable("tooltip.deadhorizon.gun.jammed").formatted(Formatting.RED));
        tooltip.add(Text.translatable("tooltip.deadhorizon.gun.keys").formatted(Formatting.DARK_GRAY));
    }
}
