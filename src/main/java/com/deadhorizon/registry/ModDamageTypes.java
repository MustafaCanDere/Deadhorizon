package com.deadhorizon.registry;

import com.deadhorizon.DeadHorizon;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

/** Damage types live in data/deadhorizon/damage_type; these are just their keys. */
public final class ModDamageTypes {
    public static final RegistryKey<DamageType> INFECTION =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, DeadHorizon.id("infection"));
    public static final RegistryKey<DamageType> BLEEDING =
            RegistryKey.of(RegistryKeys.DAMAGE_TYPE, DeadHorizon.id("bleeding"));

    private ModDamageTypes() {
    }

    public static DamageSource of(World world, RegistryKey<DamageType> key) {
        return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key));
    }
}
