package com.deadhorizon.registry;

import com.deadhorizon.DeadHorizon;
import com.deadhorizon.block.StorageBlock;
import com.deadhorizon.block.entity.StorageBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModBlockEntities {
    public static final BlockEntityType<StorageBlockEntity> STORAGE = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            DeadHorizon.id("storage"),
            FabricBlockEntityTypeBuilder.create(StorageBlockEntity::new,
                    ModBlocks.ALL.stream().filter(b -> b instanceof StorageBlock).toArray(Block[]::new)).build());

    private ModBlockEntities() {
    }

    public static void register() {
    }
}
