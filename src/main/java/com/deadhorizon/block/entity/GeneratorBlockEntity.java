package com.deadhorizon.block.entity;

import com.deadhorizon.block.GeneratorBlock;
import com.deadhorizon.config.DHConfig;
import com.deadhorizon.noise.NoiseSystem;
import com.deadhorizon.power.PowerGrid;
import com.deadhorizon.registry.ModBlockEntities;
import com.deadhorizon.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class GeneratorBlockEntity extends BlockEntity {
    /** Ticks of running time per fuel item. Coal: 1 minute, a jerry can: 5 minutes. */
    private static final int COAL_TICKS = 1200;
    private static final int JERRY_CAN_TICKS = 6000;
    private static final int MAX_FUEL = 24000;

    private int fuel;

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GENERATOR, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, GeneratorBlockEntity generator) {
        boolean running = generator.fuel > 0;
        if (running) {
            generator.fuel--;
            PowerGrid.announce(world, pos);
            long time = world.getTime();
            if (time % 40 == 0 && world instanceof ServerWorld server) {
                world.playSound(null, pos, SoundEvents.ENTITY_MINECART_RIDING, SoundCategory.BLOCKS, 0.6f, 0.5f);
                NoiseSystem.emit(server, Vec3d.ofCenter(pos), DHConfig.INSTANCE.generatorNoiseRadius);
                if (DHConfig.INSTANCE.generatorFumes && !world.isSkyVisible(pos.up())) {
                    poisonAir(world, pos);
                }
            }
            if (time % 20 == 0) generator.markDirty();
        }
        boolean lit = generator.fuel > 0;
        if (state.get(GeneratorBlock.LIT) != lit) {
            world.setBlockState(pos, state.with(GeneratorBlock.LIT, lit), Block.NOTIFY_ALL);
            world.playSound(null, pos, lit ? SoundEvents.BLOCK_PISTON_EXTEND : SoundEvents.BLOCK_PISTON_CONTRACT,
                    SoundCategory.BLOCKS, 0.5f, 0.6f);
            generator.markDirty();
        }
    }

    /** Carbon monoxide: an indoor generator makes everyone close to it sick. */
    private static void poisonAir(World world, BlockPos pos) {
        for (PlayerEntity player : world.getEntitiesByClass(PlayerEntity.class, new Box(pos).expand(5),
                p -> !p.isCreative() && !p.isSpectator())) {
            if (!player.hasStatusEffect(StatusEffects.NAUSEA)) {
                player.sendMessage(Text.translatable("message.deadhorizon.generator_fumes").formatted(Formatting.RED), true);
            }
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200, 0));
        }
    }

    public void use(PlayerEntity player, Hand hand) {
        ItemStack held = player.getStackInHand(hand);
        int add = held.isOf(ModItems.JERRY_CAN) ? JERRY_CAN_TICKS
                : held.isOf(Items.COAL) || held.isOf(Items.CHARCOAL) ? COAL_TICKS : 0;
        if (add > 0 && fuel + add <= MAX_FUEL) {
            fuel += add;
            if (!player.getAbilities().creativeMode) held.decrement(1);
            world.playSound(null, pos, add == JERRY_CAN_TICKS ? SoundEvents.ITEM_BUCKET_EMPTY : SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.8f, 1.0f);
            markDirty();
        }
        int minutes = (fuel + 1199) / 1200;
        player.sendMessage(fuel > 0
                ? Text.translatable("message.deadhorizon.generator_fuel", minutes)
                : Text.translatable("message.deadhorizon.generator_empty"), true);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        fuel = nbt.getInt("Fuel");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("Fuel", fuel);
    }
}
