package com.deadhorizon.survival;

import net.minecraft.nbt.NbtCompound;

/** Per-player thirst and stamina. Lives on the player through {@link SurvivalHolder}. */
public class SurvivalData {
    public static final float MAX_THIRST = 20.0f;
    public static final float MAX_STAMINA = 100.0f;

    public float thirst = MAX_THIRST;
    public float stamina = MAX_STAMINA;
    /** Set when stamina runs out; cleared once it climbs back over the recovery threshold. */
    public boolean exhausted;
    /** Ticks to wait after exertion before stamina starts coming back. */
    public int regenDelay;

    public void addThirst(float amount) {
        thirst = Math.max(0.0f, Math.min(MAX_THIRST, thirst + amount));
    }

    public void addStamina(float amount) {
        stamina = Math.max(0.0f, Math.min(MAX_STAMINA, stamina + amount));
    }

    public void copyFrom(SurvivalData other) {
        thirst = other.thirst;
        stamina = other.stamina;
        exhausted = other.exhausted;
        regenDelay = other.regenDelay;
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putFloat("Thirst", thirst);
        nbt.putFloat("Stamina", stamina);
        nbt.putBoolean("Exhausted", exhausted);
        return nbt;
    }

    public void fromNbt(NbtCompound nbt) {
        if (nbt.contains("Thirst")) thirst = nbt.getFloat("Thirst");
        if (nbt.contains("Stamina")) stamina = nbt.getFloat("Stamina");
        exhausted = nbt.getBoolean("Exhausted");
    }
}
