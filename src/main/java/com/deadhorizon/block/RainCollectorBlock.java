package com.deadhorizon.block;

import com.deadhorizon.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/** An open barrel that fills with rain. Rain water is clean: fill glass bottles from it. */
public class RainCollectorBlock extends Block {
    public static final IntProperty LEVEL = Properties.LEVEL_3;
    private static final VoxelShape SHAPE = Block.createCuboidShape(2, 0, 2, 14, 14, 14);

    public RainCollectorBlock(Settings settings) {
        super(settings.ticksRandomly());
        setDefaultState(getDefaultState().with(LEVEL, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(LEVEL) < 3 && world.hasRain(pos.up())) {
            world.setBlockState(pos, state.with(LEVEL, state.get(LEVEL) + 1), Block.NOTIFY_ALL);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack held = player.getStackInHand(hand);
        int level = state.get(LEVEL);
        if (held.isOf(Items.GLASS_BOTTLE) && level > 0) {
            if (!world.isClient) {
                player.setStackInHand(hand, ItemUsage.exchangeStack(held, player, new ItemStack(ModItems.CLEAN_WATER)));
                world.setBlockState(pos, state.with(LEVEL, level - 1), Block.NOTIFY_ALL);
                world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
            return ActionResult.success(world.isClient);
        }
        return ActionResult.PASS;
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return state.get(LEVEL);
    }
}
