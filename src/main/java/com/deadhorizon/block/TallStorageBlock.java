package com.deadhorizon.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

/** Two-block-tall storage (fridge, wardrobe); the inventory lives in the bottom half. */
public class TallStorageBlock extends StorageBlock {
    public TallStorageBlock(Settings settings, int rows, double[]... boxes) {
        super(settings, rows, Kind.NORMAL, boxes);
        setDefaultState(getDefaultState().with(TallFurnitureBlock.HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(TallFurnitureBlock.HALF);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return TallFurnitureBlock.canPlace(ctx.getWorld(), ctx.getBlockPos(), ctx) ? super.getPlacementState(ctx) : null;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onPlaced(world, pos, state, placer, stack);
        TallFurnitureBlock.placeUpper(world, pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
                                                WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        BlockState result = TallFurnitureBlock.neighborUpdate(state, direction, neighborState, this);
        return result != null ? result : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        TallFurnitureBlock.onBreakUpper(world, pos, state, player);
        super.onBreak(world, pos, state, player);
    }

    @Override
    protected BlockPos containerPos(BlockState state, BlockPos pos) {
        return TallFurnitureBlock.lowerPos(state, pos);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return TallFurnitureBlock.isLower(state) ? super.createBlockEntity(pos, state) : null;
    }
}
