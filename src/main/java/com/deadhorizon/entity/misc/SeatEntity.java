package com.deadhorizon.entity.misc;

import com.deadhorizon.block.SeatBlock;
import com.deadhorizon.registry.ModEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

/** Invisible entity a player rides while sitting on a chair; removes itself when left. */
public class SeatEntity extends Entity {
    public SeatEntity(EntityType<?> type, World world) {
        super(type, world);
        this.noClip = true;
    }

    public static void sit(World world, BlockPos pos, double height, PlayerEntity player) {
        List<SeatEntity> existing = world.getEntitiesByClass(SeatEntity.class, new Box(pos), e -> true);
        for (SeatEntity seat : existing) {
            if (seat.hasPassengers()) return;
            seat.discard();
        }
        SeatEntity seat = new SeatEntity(ModEntities.SEAT, world);
        seat.setPosition(pos.getX() + 0.5, pos.getY() + height, pos.getZ() + 0.5);
        world.spawnEntity(seat);
        player.startRiding(seat);
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClient && (!hasPassengers() || !(getWorld().getBlockState(getBlockPos()).getBlock() instanceof SeatBlock))) {
            discard();
        }
    }

    @Override
    public double getMountedHeightOffset() {
        return 0.0;
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        BlockPos pos = getBlockPos();
        BlockState state = getWorld().getBlockState(pos);
        if (state.contains(HorizontalFacingBlock.FACING)) {
            BlockPos front = pos.offset(state.get(HorizontalFacingBlock.FACING));
            if (getWorld().getBlockState(front).getCollisionShape(getWorld(), front).isEmpty()
                    && getWorld().getBlockState(front.up()).getCollisionShape(getWorld(), front.up()).isEmpty()) {
                return Vec3d.ofBottomCenter(front);
            }
        }
        return Vec3d.ofBottomCenter(pos.up());
    }

    @Override
    protected void initDataTracker() {
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }
}
