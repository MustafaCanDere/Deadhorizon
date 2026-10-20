package com.deadhorizon.weapon;

import com.deadhorizon.DeadHorizon;
import com.deadhorizon.config.DHConfig;
import com.deadhorizon.noise.NoiseSystem;
import com.deadhorizon.registry.ModDamageTypes;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;

/** Server side of the firearms: hitscan shots, reloading, jams and fire mode. */
public final class Guns {
    public static final Identifier RELOAD = DeadHorizon.id("gun_reload");
    public static final Identifier FIRE_MODE = DeadHorizon.id("gun_fire_mode");
    public static final Identifier RECOIL = DeadHorizon.id("gun_recoil");

    private Guns() {
    }

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(RELOAD, (server, player, handler, buf, sender) ->
                server.execute(() -> startReload(player)));
        ServerPlayNetworking.registerGlobalReceiver(FIRE_MODE, (server, player, handler, buf, sender) ->
                server.execute(() -> toggleFireMode(player)));
    }

    public static void tryFire(ServerPlayerEntity player, ItemStack stack, GunItem gun) {
        GunSpec spec = gun.spec();
        ServerWorld world = player.getServerWorld();
        if (GunItem.reloadUntil(stack) > 0) return;
        if (GunItem.isJammed(stack)) {
            click(player);
            return;
        }
        int ammo = GunItem.ammo(stack);
        if (ammo <= 0 && !player.isCreative()) {
            click(player);
            player.sendMessage(Text.translatable("message.deadhorizon.gun.empty").formatted(Formatting.GRAY), true);
            player.stopUsingItem();
            return;
        }

        // A worn-out gun jams now and then; R clears it.
        if (stack.isDamageable() && stack.getDamage() > stack.getMaxDamage() * 0.7
                && player.getRandom().nextFloat() < DHConfig.INSTANCE.gunJamChance) {
            GunItem.setJammed(stack, true);
            world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_IRON_DOOR_CLOSE, SoundCategory.PLAYERS, 0.7f, 1.8f);
            player.sendMessage(Text.translatable("message.deadhorizon.gun.jammed").formatted(Formatting.RED), true);
            player.stopUsingItem();
            return;
        }

        if (!player.isCreative()) GunItem.setAmmo(stack, ammo - 1);
        stack.damage(1, player, p -> p.sendToolBreakStatus(p.getActiveHand()));

        float spread = spec.spread();
        if (player.isSneaking()) spread *= GunItem.has(stack, AttachmentItem.Type.SCOPE) ? 0.2f : 0.4f; // aiming down the sights
        if (player.isSprinting()) spread *= 1.8f;
        else if (player.getVelocity().horizontalLengthSquared() > 0.003) spread *= 1.4f;
        if (!player.isOnGround()) spread *= 2.0f;

        Vec3d eye = player.getEyePos();
        for (int i = 0; i < spec.pellets(); i++) {
            shoot(player, world, eye, direction(player, spread, player.getRandom()), spec, 1.0f);
        }

        muzzle(world, player, spec, GunItem.has(stack, AttachmentItem.Type.SUPPRESSOR));
        NoiseSystem.emit(world, player.getPos(), gun.noise(stack));

        float recoil = spec.recoil() * (player.isSneaking() ? 0.6f : 1.0f);
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeFloat(recoil);
        buf.writeFloat((player.getRandom().nextFloat() - 0.5f) * recoil * 0.5f);
        ServerPlayNetworking.send(player, RECOIL, buf);
    }

    /** Muzzle smoke, flash and the report, for players and NPCs alike. */
    public static void muzzle(ServerWorld world, LivingEntity player, GunSpec spec, boolean suppressed) {
        Vec3d muzzle = player.getEyePos().add(player.getRotationVec(1.0f).multiply(0.9)).add(0, -0.15, 0);
        world.spawnParticles(ParticleTypes.SMOKE, muzzle.x, muzzle.y, muzzle.z, 3, 0.03, 0.03, 0.03, 0.01);
        world.spawnParticles(ParticleTypes.FLAME, muzzle.x, muzzle.y, muzzle.z, 1, 0.0, 0.0, 0.0, 0.0);
        boolean shotgun = spec.pellets() > 1;
        if (suppressed) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ARROW_SHOOT,
                    SoundCategory.PLAYERS, 0.8f, 1.6f + player.getRandom().nextFloat() * 0.2f);
        } else {
            world.playSound(null, player.getX(), player.getY(), player.getZ(),
                    shotgun ? SoundEvents.ENTITY_GENERIC_EXPLODE : SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST,
                    SoundCategory.PLAYERS, shotgun ? 1.2f : 2.5f, shotgun ? 1.9f : 0.55f + player.getRandom().nextFloat() * 0.1f);
        }
        if (!(player instanceof ServerPlayerEntity)) NoiseSystem.emit(world, player.getPos(), spec.noise());
    }

    private static Vec3d direction(ServerPlayerEntity player, float spread, Random random) {
        float pitch = player.getPitch() + (float) random.nextGaussian() * spread * 0.5f;
        float yaw = player.getYaw() + (float) random.nextGaussian() * spread * 0.5f;
        return Vec3d.fromPolar(MathHelper.clamp(pitch, -90.0f, 90.0f), yaw);
    }

    /** One hitscan round from any shooter (players and armed NPCs). */
    public static void shoot(LivingEntity player, ServerWorld world, Vec3d eye, Vec3d dir, GunSpec spec, float damageScale) {
        Vec3d end = eye.add(dir.multiply(spec.range()));
        BlockHitResult block = world.raycast(new RaycastContext(eye, end, RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, player));
        Vec3d stop = block.getType() == HitResult.Type.MISS ? end : block.getPos();
        Box sweep = player.getBoundingBox().stretch(dir.multiply(spec.range())).expand(1.0);
        EntityHitResult hit = ProjectileUtil.raycast(player, eye, stop, sweep,
                e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator() && e.canHit(), spec.range() * spec.range());

        Vec3d impact = hit != null ? hit.getPos() : stop;
        tracer(world, eye.add(dir.multiply(1.5)), impact);

        if (hit != null) {
            Entity target = hit.getEntity();
            float damage = spec.damage() * damageScale;
            boolean headshot = impact.y >= target.getY() + target.getHeight() * 0.78;
            if (headshot && DHConfig.INSTANCE.headshots) damage *= (float) DHConfig.INSTANCE.headshotMultiplier;
            // Each round is its own hit: don't let the hurt cooldown swallow full-auto fire or pellets.
            target.timeUntilRegen = 0;
            target.damage(bullet(world, player), damage);
            target.timeUntilRegen = 0;
            if (headshot) {
                world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 0.4f, 0.6f);
            }
        } else if (block.getType() == HitResult.Type.BLOCK) {
            BlockState state = world.getBlockState(block.getBlockPos());
            world.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state),
                    impact.x, impact.y, impact.z, 6, 0.05, 0.05, 0.05, 0.1);
            if (DHConfig.INSTANCE.bulletsBreakGlass && state.getSoundGroup() == BlockSoundGroup.GLASS) {
                world.breakBlock(block.getBlockPos(), false, player);
            }
        }
    }

    private static void tracer(ServerWorld world, Vec3d from, Vec3d to) {
        Vec3d step = to.subtract(from);
        double length = step.length();
        if (length < 0.5) return;
        int points = (int) Math.min(40, length / 1.5);
        for (int i = 0; i <= points; i++) {
            Vec3d p = from.add(step.multiply((double) i / Math.max(1, points)));
            world.spawnParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
    }

    private static DamageSource bullet(ServerWorld world, LivingEntity shooter) {
        return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(ModDamageTypes.BULLET),
                shooter, shooter);
    }

    private static void click(ServerPlayerEntity player) {
        player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 0.6f, 1.8f);
    }

    public static void startReload(ServerPlayerEntity player) {
        ItemStack stack = player.getMainHandStack();
        if (!(stack.getItem() instanceof GunItem gun) || GunItem.reloadUntil(stack) > 0) return;
        ServerWorld world = player.getServerWorld();
        if (GunItem.isJammed(stack)) {
            // Clearing a jam is quicker than a full reload.
            GunItem.setJammed(stack, false);
            player.getItemCooldownManager().set(gun, 20);
            world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.PLAYERS, 0.8f, 1.6f);
            player.sendMessage(Text.translatable("message.deadhorizon.gun.unjammed").formatted(Formatting.GRAY), true);
            return;
        }
        if (GunItem.ammo(stack) >= gun.magazine(stack)) return;
        if (!player.isCreative() && count(player.getInventory(), gun.spec().ammo().get()) == 0) {
            player.sendMessage(Text.translatable("message.deadhorizon.gun.no_ammo").formatted(Formatting.GRAY), true);
            return;
        }
        player.stopUsingItem();
        GunItem.setReloadUntil(stack, world.getTime() + gun.spec().reloadTicks());
        world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_CROSSBOW_LOADING_START, SoundCategory.PLAYERS, 1.0f, 0.8f);
    }

    public static void finishReload(ServerPlayerEntity player, ItemStack stack, GunItem gun) {
        int needed = gun.magazine(stack) - GunItem.ammo(stack);
        int taken = player.isCreative() ? needed : take(player.getInventory(), gun.spec().ammo().get(), needed);
        GunItem.setAmmo(stack, GunItem.ammo(stack) + taken);
        player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ITEM_CROSSBOW_LOADING_END,
                SoundCategory.PLAYERS, 1.0f, 1.1f);
    }

    private static void toggleFireMode(ServerPlayerEntity player) {
        ItemStack stack = player.getMainHandStack();
        if (!(stack.getItem() instanceof GunItem gun) || !gun.spec().automatic()) return;
        boolean auto = !gun.isAuto(stack);
        GunItem.setAuto(stack, auto);
        player.stopUsingItem();
        player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.PLAYERS, 0.5f, 1.5f);
        player.sendMessage(Text.translatable(auto ? "tooltip.deadhorizon.gun.auto" : "tooltip.deadhorizon.gun.semi"), true);
    }

    public static int count(PlayerInventory inventory, Item item) {
        int total = 0;
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack s = inventory.getStack(i);
            if (s.isOf(item)) total += s.getCount();
        }
        return total;
    }

    private static int take(PlayerInventory inventory, Item item, int wanted) {
        int taken = 0;
        for (int i = 0; i < inventory.size() && taken < wanted; i++) {
            ItemStack s = inventory.getStack(i);
            if (!s.isOf(item)) continue;
            int n = Math.min(wanted - taken, s.getCount());
            s.decrement(n);
            taken += n;
        }
        return taken;
    }
}
