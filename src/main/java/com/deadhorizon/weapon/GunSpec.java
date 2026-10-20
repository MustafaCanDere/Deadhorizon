package com.deadhorizon.weapon;

import java.util.function.Supplier;

import net.minecraft.item.Item;

/**
 * Everything that makes one gun different from another.
 *
 * @param ammo          cartridge item it loads
 * @param magazine      rounds per magazine
 * @param damage        damage per pellet/bullet before headshot bonus
 * @param pellets       projectiles per shot (shotgun > 1)
 * @param interval      ticks between shots
 * @param automatic     can fire full auto (holding right click)
 * @param spread        inaccuracy in degrees when standing still from the hip
 * @param recoil        upward camera kick in degrees
 * @param range         blocks
 * @param reloadTicks   time to reload
 * @param noise         radius in blocks at which zombies hear it
 * @param zoom          FOV multiplier while aiming (sneaking)
 */
public record GunSpec(Supplier<Item> ammo, int magazine, float damage, int pellets, int interval, boolean automatic,
                      float spread, float recoil, double range, int reloadTicks, double noise, float zoom) {
}
