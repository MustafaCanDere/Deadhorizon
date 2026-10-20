package com.deadhorizon.block;

import com.deadhorizon.power.PowerGrid;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

/** A device that checks every two seconds whether a power source is in range and sets POWERED to match. */
public class ElectricDeviceBlock extends FurnitureBlock {
    public static final BooleanProperty POWERED = Properties.POWERED;
    private static final int CHECK_TICKS = 40;

    public ElectricDeviceBlock(Settings settings, double[]... boxes) {
        super(settings, boxes);
        setDefaultState(getDefaultState().with(POWERED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(POWERED);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!world.isClient && !oldState.isOf(this)) {
            world.scheduleBlockTick(pos, this, 1);
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        boolean powered = PowerGrid.isPowered(world, pos);
        if (state.get(POWERED) != powered) {
            world.setBlockState(pos, state.with(POWERED, powered), Block.NOTIFY_ALL);
        }
        world.scheduleBlockTick(pos, this, CHECK_TICKS);
    }
}
