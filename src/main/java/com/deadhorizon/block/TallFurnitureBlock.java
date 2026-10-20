package com.deadhorizon.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/** Two-block-tall helpers shared by tall furniture; mirrors how doors keep their halves together. */
public final class TallFurnitureBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;

    private TallFurnitureBlock() {
    }

    public static boolean canPlace(World world, BlockPos pos, ItemPlacementContext ctx) {
        return pos.getY() < world.getTopY() - 1 && world.getBlockState(pos.up()).canReplace(ctx);
    }

    public static void placeUpper(World world, BlockPos pos, BlockState state) {
        world.setBlockState(pos.up(), state.with(HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_ALL);
    }

    /** Returns AIR when the other half has gone. */
    public static BlockState neighborUpdate(BlockState state, Direction direction, BlockState neighbor, Block self) {
        DoubleBlockHalf half = state.get(HALF);
        if (direction.getAxis() == Direction.Axis.Y && (half == DoubleBlockHalf.LOWER) == (direction == Direction.UP)) {
            return neighbor.isOf(self) && neighbor.get(HALF) != half ? state : Blocks.AIR.getDefaultState();
        }
        return null;
    }

    /** Breaking the top half in survival drops the item from the bottom half (whose loot table owns it). */
    public static void onBreakUpper(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (world.isClient || player.isCreative() || state.get(HALF) != DoubleBlockHalf.UPPER) return;
        BlockPos lower = pos.down();
        BlockState lowerState = world.getBlockState(lower);
        if (lowerState.isOf(state.getBlock())) {
            Block.dropStacks(lowerState, world, lower, world.getBlockEntity(lower), player, player.getMainHandStack());
        }
    }

    public static BlockPos lowerPos(BlockState state, BlockPos pos) {
        return state.get(HALF) == DoubleBlockHalf.UPPER ? pos.down() : pos;
    }

    public static boolean isLower(BlockState state) {
        return state.get(HALF) == DoubleBlockHalf.LOWER;
    }
}
