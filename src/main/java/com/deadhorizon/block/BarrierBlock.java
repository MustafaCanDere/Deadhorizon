package com.deadhorizon.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

import java.util.Map;

/** Concrete road barrier: one block to look at, a block and a half to jump, so zombies can't. */
public class BarrierBlock extends FurnitureBlock {
    private static final Map<Direction, VoxelShape> COLLISION = Shapes.horizontal(Shapes.box(1, 0, 3, 15, 24, 13));

    public BarrierBlock(Settings settings) {
        super(settings, Shapes.box(1, 0, 3, 15, 16, 13));
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return COLLISION.get(state.get(FACING));
    }
}
