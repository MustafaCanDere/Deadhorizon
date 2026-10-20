package com.deadhorizon.survival;

import net.minecraft.nbt.NbtCompound;

/** Per-player thirst, stamina, body temperature and rest. Lives on the player through {@link SurvivalHolder}. */
public class SurvivalData {
    public static final float MAX_THIRST = 20.0f;
    public static final float MAX_STAMINA = 100.0f;
    public static final float NORMAL_TEMPERATURE = 50.0f;
    public static final float MAX_REST = 100.0f;

    public float thirst = MAX_THIRST;
    public float stamina = MAX_STAMINA;
    /** Set when stamina runs out; cleared once it climbs back over the recovery threshold. */
    public boolean exhausted;
    /** Ticks to wait after exertion before stamina starts coming back. */
    public int regenDelay;
    /** 0 = freezing, 50 = comfortable, 100 = heatstroke. Drifts towards the surroundings. */
    public float temperature = NORMAL_TEMPERATURE;
    /** 100 = well rested, 0 = falling asleep on your feet. Refilled by sleeping. */
    public float rest = MAX_REST;
    /** 0 = light, 1 = heavy, 2 = overloaded. Recomputed from the inventory, not saved. */
    public int load;

    public void addThirst(float amount) {
        thirst = Math.max(0.0f, Math.min(MAX_THIRST, thirst + amount));
    }

    public void addStamina(float amount) {
        stamina = Math.max(0.0f, Math.min(MAX_STAMINA, stamina + amount));
    }

    public void addRest(float amount) {
        rest = Math.max(0.0f, Math.min(MAX_REST, rest + amount));
    }

    public void copyFrom(SurvivalData other) {
        temperature = other.temperature;
        rest = other.rest;
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
        nbt.putFloat("Temperature", temperature);
        nbt.putFloat("Rest", rest);
        return nbt;
    }

    public void fromNbt(NbtCompound nbt) {
        if (nbt.contains("Thirst")) thirst = nbt.getFloat("Thirst");
        if (nbt.contains("Stamina")) stamina = nbt.getFloat("Stamina");
        exhausted = nbt.getBoolean("Exhausted");
        if (nbt.contains("Temperature")) temperature = nbt.getFloat("Temperature");
        if (nbt.contains("Rest")) rest = nbt.getFloat("Rest");
    }
}
