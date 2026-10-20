package com.deadhorizon.block;

import com.deadhorizon.entity.misc.SeatEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Right-click to sit. */
public class SeatBlock extends FurnitureBlock {
    private final double seatHeight;

    public SeatBlock(Settings settings, double seatHeight, double[]... boxes) {
        super(settings, boxes);
        this.seatHeight = seatHeight;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.isSneaking() || player.hasVehicle()) return ActionResult.PASS;
        if (!world.isClient) {
            SeatEntity.sit(world, pos, seatHeight, player);
        }
        return ActionResult.success(world.isClient);
    }
}
