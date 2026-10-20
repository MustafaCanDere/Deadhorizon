package com.deadhorizon.event;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.Nullable;

/** Saved with the overworld: which day was last announced, horde timing and the last supply drop. */
public class ApocalypseState extends PersistentState {
    private static final String ID = "deadhorizon_apocalypse";

    public long lastAnnouncedDay = -1;
    public long nextHordeTime = -1;
    public long lastDropDay = -1;
    @Nullable
    public BlockPos lastDrop;
    public boolean bloodMoon;

    public static ApocalypseState get(ServerWorld overworld) {
        return overworld.getPersistentStateManager().getOrCreate(ApocalypseState::fromNbt, ApocalypseState::new, ID);
    }

    private static ApocalypseState fromNbt(NbtCompound nbt) {
        ApocalypseState state = new ApocalypseState();
        state.lastAnnouncedDay = nbt.getLong("LastAnnouncedDay");
        state.nextHordeTime = nbt.getLong("NextHordeTime");
        state.lastDropDay = nbt.getLong("LastDropDay");
        if (nbt.contains("LastDrop")) state.lastDrop = BlockPos.fromLong(nbt.getLong("LastDrop"));
        state.bloodMoon = nbt.getBoolean("BloodMoon");
        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putLong("LastAnnouncedDay", lastAnnouncedDay);
        nbt.putLong("NextHordeTime", nextHordeTime);
        nbt.putLong("LastDropDay", lastDropDay);
        if (lastDrop != null) nbt.putLong("LastDrop", lastDrop.asLong());
        nbt.putBoolean("BloodMoon", bloodMoon);
        return nbt;
    }
}
