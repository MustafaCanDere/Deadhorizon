package com.deadhorizon.registry;

import com.deadhorizon.DeadHorizon;
import com.deadhorizon.block.BarbedWireBlock;
import com.deadhorizon.block.BarricadeBlock;
import com.deadhorizon.block.BarrierBlock;
import com.deadhorizon.block.FurnitureBlock;
import com.deadhorizon.block.LampBlock;
import com.deadhorizon.block.RainCollectorBlock;
import com.deadhorizon.block.SeatBlock;
import com.deadhorizon.block.StorageBlock;
import com.deadhorizon.block.StorageBlock.Kind;
import com.deadhorizon.block.TallStorageBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

import java.util.ArrayList;
import java.util.List;

import static com.deadhorizon.block.Shapes.box;

public final class ModBlocks {
    public static final List<Block> ALL = new ArrayList<>();

    // Interior storage
    public static final Block KITCHEN_CABINET = register("kitchen_cabinet",
            new StorageBlock(wood(), 3, Kind.NORMAL, box(0, 0, 0, 16, 16, 16)));
    public static final Block FRIDGE = register("fridge",
            new TallStorageBlock(metal(MapColor.WHITE), 3, box(1, 0, 1, 15, 16, 15)));
    public static final Block WARDROBE = register("wardrobe",
            new TallStorageBlock(wood().mapColor(MapColor.BROWN), 6, box(0, 0, 1, 16, 16, 16)));
    public static final Block MEDICINE_CABINET = register("medicine_cabinet",
            new StorageBlock(metal(MapColor.WHITE), 1, Kind.NORMAL, box(2, 2, 10.5, 14, 14, 16)));
    public static final Block SAFE = register("safe",
            new StorageBlock(metal(MapColor.GRAY).strength(25.0f, 1200.0f), 3, Kind.SAFE, box(1, 0, 1, 15, 14, 15)));
    public static final Block TRASH_CAN = register("trash_can",
            new StorageBlock(metal(MapColor.GRAY), 1, Kind.TRASH, box(2.5, 0, 2.5, 13.5, 13, 13.5)));
    public static final Block CRATE = register("crate",
            new StorageBlock(wood(), 3, Kind.NORMAL, box(1, 0, 1, 15, 15, 15)));

    // Seating and tables
    public static final Block CHAIR = register("chair",
            new SeatBlock(wood(), 0.5, box(2, 0, 2, 14, 8, 14), box(2, 8, 12, 14, 16, 14)));
    public static final Block STOOL = register("stool",
            new SeatBlock(wood(), 0.6, box(4, 0, 4, 12, 10, 12)));
    public static final Block SOFA = register("sofa",
            new SeatBlock(wood().mapColor(MapColor.RED).sounds(BlockSoundGroup.WOOL), 0.5,
                    box(0, 0, 2, 16, 8, 16), box(0, 8, 13, 16, 16, 16)));
    public static final Block TABLE = register("table",
            new FurnitureBlock(wood(), box(0, 14, 0, 16, 16, 16), box(1, 0, 1, 3, 14, 3), box(13, 0, 1, 15, 14, 3),
                    box(1, 0, 13, 3, 14, 15), box(13, 0, 13, 15, 14, 15)));

    // Lights
    public static final Block TABLE_LAMP = register("table_lamp",
            new LampBlock(lamp(), box(5, 0, 5, 11, 12, 11)));
    public static final Block CEILING_LAMP = register("ceiling_lamp",
            new LampBlock(lamp(), box(4, 9, 4, 12, 16, 12)));
    public static final Block STREET_LAMP = register("street_lamp",
            new LampBlock(lamp().strength(2.0f), box(7, 0, 7, 9, 10, 9), box(4, 10, 4, 12, 16, 12)));

    // Exterior and defence
    public static final Block DUMPSTER = register("dumpster",
            new StorageBlock(metal(MapColor.DARK_GREEN), 6, Kind.NORMAL, box(0, 0, 0, 16, 14, 16)));
    public static final Block SANDBAGS = register("sandbags",
            new FurnitureBlock(AbstractBlock.Settings.create().mapColor(MapColor.PALE_YELLOW).strength(2.5f, 30.0f)
                    .sounds(BlockSoundGroup.WOOL), box(0, 0, 0, 16, 16, 16)));
    public static final Block BARBED_WIRE = register("barbed_wire",
            new BarbedWireBlock(AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).strength(1.0f)
                    .sounds(BlockSoundGroup.CHAIN).noCollision().nonOpaque()));
    public static final Block BARRICADE = register("barricade",
            new BarricadeBlock(wood().strength(3.0f, 4.0f)));
    public static final Block RAIN_COLLECTOR = register("rain_collector",
            new RainCollectorBlock(wood().nonOpaque()));
    public static final Block TRAFFIC_CONE = register("traffic_cone",
            new FurnitureBlock(AbstractBlock.Settings.create().mapColor(MapColor.ORANGE).strength(0.5f)
                    .sounds(BlockSoundGroup.STONE).nonOpaque(), box(3, 0, 3, 13, 12, 13)));
    public static final Block CONCRETE_BARRIER = register("concrete_barrier",
            new BarrierBlock(AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY).strength(3.0f, 9.0f)
                    .sounds(BlockSoundGroup.STONE).nonOpaque()));

    private ModBlocks() {
    }

    private static AbstractBlock.Settings wood() {
        return AbstractBlock.Settings.create().mapColor(MapColor.OAK_TAN).strength(2.0f).sounds(BlockSoundGroup.WOOD)
                .nonOpaque().burnable();
    }

    private static AbstractBlock.Settings metal(MapColor color) {
        return AbstractBlock.Settings.create().mapColor(color).strength(3.0f, 6.0f).sounds(BlockSoundGroup.METAL).nonOpaque();
    }

    private static AbstractBlock.Settings lamp() {
        return AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).strength(0.5f).sounds(BlockSoundGroup.LANTERN)
                .nonOpaque();
    }

    private static Block register(String name, Block block) {
        Registry.register(Registries.BLOCK, DeadHorizon.id(name), block);
        Registry.register(Registries.ITEM, DeadHorizon.id(name), new BlockItem(block, new Item.Settings()));
        ALL.add(block);
        return block;
    }

    public static void register() {
    }
}
