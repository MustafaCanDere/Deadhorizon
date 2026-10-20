package com.deadhorizon.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

/**
 * Boards nailed across a window or doorway. Zombies that cannot reach their target batter it
 * (see BreakBarricadeGoal); each hit adds damage until it gives way. Planks repair it.
 */
public class BarricadeBlock extends FurnitureBlock {
    public static final int MAX_DAMAGE = 3;
    public static final IntProperty DAMAGE = IntProperty.of("damage", 0, MAX_DAMAGE);

    public BarricadeBlock(Settings settings) {
        super(settings, Shapes.box(0, 0, 6.5, 16, 16, 9.5));
        setDefaultState(getDefaultState().with(DAMAGE, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(DAMAGE);
    }

    /** Applies zombie damage; returns true if the barricade broke. */
    public static boolean hit(World world, BlockPos pos, int amount) {
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BarricadeBlock)) return false;
        int damage = state.get(DAMAGE) + amount;
        if (damage > MAX_DAMAGE) {
            world.syncWorldEvent(WorldEvents.ZOMBIE_BREAKS_WOODEN_DOOR, pos, 0);
            world.breakBlock(pos, false);
            return true;
        }
        world.syncWorldEvent(WorldEvents.ZOMBIE_ATTACKS_WOODEN_DOOR, pos, 0);
        world.setBlockState(pos, state.with(DAMAGE, damage), Block.NOTIFY_ALL);
        return false;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(hand);
        if (!stack.isIn(ItemTags.PLANKS) || state.get(DAMAGE) == 0) return ActionResult.PASS;
        if (!world.isClient) {
            world.setBlockState(pos, state.with(DAMAGE, state.get(DAMAGE) - 1), Block.NOTIFY_ALL);
            world.playSound(null, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0f, 0.8f);
            if (!player.getAbilities().creativeMode) stack.decrement(1);
        }
        return ActionResult.success(world.isClient);
    }
}
