package com.deadhorizon.block.entity;

import com.deadhorizon.block.StorageBlock;
import com.deadhorizon.food.Spoilage;
import com.deadhorizon.registry.ModBlocks;
import com.deadhorizon.registry.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/** Inventory for every StorageBlock; its size comes from the block (1, 3 or 6 rows). */
public class StorageBlockEntity extends LootableContainerBlockEntity {
    private final int rows;
    private DefaultedList<ItemStack> items;
    @Nullable
    private UUID owner;
    /** Fridge only: world time it was last closed, or -1. Food inside ages at a quarter speed meanwhile. */
    private long closedAt = -1L;

    public StorageBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STORAGE, pos, state);
        this.rows = state.getBlock() instanceof StorageBlock storage ? storage.rows() : 3;
        this.items = DefaultedList.ofSize(rows * 9, ItemStack.EMPTY);
    }

    public boolean hasUnsearchedLoot() {
        return this.lootTableId != null;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        markDirty();
    }

    public boolean canOpen(PlayerEntity player) {
        return owner == null || owner.equals(player.getUuid()) || (player.isCreative() && player.hasPermissionLevel(2));
    }

    @Override
    public int size() {
        return rows * 9;
    }

    @Override
    protected DefaultedList<ItemStack> getInvStackList() {
        return items;
    }

    @Override
    protected void setInvStackList(DefaultedList<ItemStack> list) {
        this.items = list;
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        ScreenHandlerType<?> type = switch (rows) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 6 -> ScreenHandlerType.GENERIC_9X6;
            default -> ScreenHandlerType.GENERIC_9X3;
        };
        return new GenericContainerScreenHandler(type, syncId, playerInventory, this, rows);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.items = DefaultedList.ofSize(size(), ItemStack.EMPTY);
        if (!deserializeLootTable(nbt)) {
            Inventories.readNbt(nbt, items);
        }
        this.owner = nbt.containsUuid("Owner") ? nbt.getUuid("Owner") : null;
        this.closedAt = nbt.contains("ClosedAt") ? nbt.getLong("ClosedAt") : -1L;
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (!serializeLootTable(nbt)) {
            Inventories.writeNbt(nbt, items);
        }
        if (owner != null) nbt.putUuid("Owner", owner);
        if (closedAt >= 0) nbt.putLong("ClosedAt", closedAt);
    }

    @Override
    public void onOpen(PlayerEntity player) {
        if (player.isSpectator()) return;
        playSound(true);
        if (isFridge() && world != null && closedAt >= 0) {
            long credit = (world.getTime() - closedAt) * 3 / 4;
            for (ItemStack stack : items) Spoilage.preserve(stack, credit);
            closedAt = -1L;
            markDirty();
        }
    }

    @Override
    public void onClose(PlayerEntity player) {
        if (player.isSpectator()) return;
        playSound(false);
        if (getCachedState().getBlock() instanceof StorageBlock storage && storage.kind() == StorageBlock.Kind.TRASH) {
            clear();
            markDirty();
        }
        if (isFridge() && world != null) {
            closedAt = world.getTime();
            markDirty();
        }
    }

    private boolean isFridge() {
        return getCachedState().isOf(ModBlocks.FRIDGE);
    }

    private void playSound(boolean open) {
        if (world == null) return;
        boolean metal = getCachedState().getSoundGroup() == BlockSoundGroup.METAL;
        SoundEvent sound = metal
                ? (open ? SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN : SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE)
                : (open ? SoundEvents.BLOCK_BARREL_OPEN : SoundEvents.BLOCK_BARREL_CLOSE);
        world.playSound(null, pos, sound, SoundCategory.BLOCKS, 0.5f, world.random.nextFloat() * 0.1f + 0.9f);
    }
}
