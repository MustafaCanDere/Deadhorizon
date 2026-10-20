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

/** Silent free power, but only by day, under open sky and without rain. */
public class SolarPanelBlock extends FurnitureBlock implements PowerGrid.Source {
    public static final BooleanProperty POWERED = Properties.POWERED;
    private static final int CHECK_TICKS = 100;

    public SolarPanelBlock(Settings settings, double[]... boxes) {
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
        boolean sun = world.isDay() && !world.isRaining() && world.isSkyVisible(pos.up());
        if (state.get(POWERED) != sun) {
            world.setBlockState(pos, state.with(POWERED, sun), Block.NOTIFY_ALL);
        }
        if (sun) PowerGrid.announce(world, pos);
        world.scheduleBlockTick(pos, this, CHECK_TICKS);
    }
}
