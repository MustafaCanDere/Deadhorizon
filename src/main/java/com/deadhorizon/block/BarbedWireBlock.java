package com.deadhorizon.block;

import com.deadhorizon.registry.ModDamageTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/** Slows and cuts anything walking through it, zombies included. */
public class BarbedWireBlock extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 8, 16);

    public BarbedWireBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!(entity instanceof LivingEntity living)) return;
        if (living instanceof PlayerEntity player && (player.isCreative() || player.isSpectator())) return;
        entity.slowMovement(state, new Vec3d(0.45, 0.7, 0.45));
        if (!world.isClient) {
            living.damage(ModDamageTypes.of(world, ModDamageTypes.BARBED_WIRE), 1.0f);
        }
    }
}
