package com.deadhorizon.block;

import com.deadhorizon.block.entity.StorageBlockEntity;
import com.deadhorizon.loot.LootSearch;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Furniture with an inventory (cabinets, crates, safes...). If the block entity still holds an
 * unrolled loot table, opening it first takes a short search instead of opening instantly.
 */
public class StorageBlock extends FurnitureBlock implements BlockEntityProvider {
    public enum Kind {
        NORMAL,
        /** Destroys whatever is left inside when closed. */
        TRASH,
        /** Only the player who placed it can open or break it. */
        SAFE
    }

    private final int rows;
    private final Kind kind;

    public StorageBlock(Settings settings, int rows, Kind kind, double[]... boxes) {
        super(settings, boxes);
        this.rows = rows;
        this.kind = kind;
    }

    public int rows() {
        return rows;
    }

    public Kind kind() {
        return kind;
    }

    /** The position that holds the inventory (differs for the top half of tall furniture). */
    protected BlockPos containerPos(BlockState state, BlockPos pos) {
        return pos;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new StorageBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;
        BlockPos at = containerPos(state, pos);
        if (!(world.getBlockEntity(at) instanceof StorageBlockEntity storage)) return ActionResult.PASS;
        if (!storage.canOpen(player)) {
            player.sendMessage(Text.translatable("message.deadhorizon.locked").formatted(Formatting.RED), true);
            return ActionResult.CONSUME;
        }
        if (storage.hasUnsearchedLoot() && player instanceof ServerPlayerEntity serverPlayer) {
            LootSearch.start(serverPlayer, at);
        } else {
            player.openHandledScreen(storage);
        }
        return ActionResult.CONSUME;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onPlaced(world, pos, state, placer, stack);
        if (world.getBlockEntity(pos) instanceof StorageBlockEntity storage) {
            if (stack.hasCustomName()) storage.setCustomName(stack.getName());
            if (kind == Kind.SAFE && placer instanceof PlayerEntity player) storage.setOwner(player.getUuid());
        }
    }

    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        if (kind == Kind.SAFE && world.getBlockEntity(containerPos(state, pos)) instanceof StorageBlockEntity storage
                && !storage.canOpen(player)) {
            return 0.0f;
        }
        return super.calcBlockBreakingDelta(state, player, world, pos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (world.getBlockEntity(pos) instanceof StorageBlockEntity storage) {
                ItemScatterer.spawn(world, pos, storage);
                world.updateComparators(pos, this);
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(containerPos(state, pos)));
    }
}
