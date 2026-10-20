package com.deadhorizon.stalker;

import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

/** A piece of a protective suit: each worn piece blocks about a fifth of incoming radiation. Hazard yellow. */
public class HazmatItem extends DyeableArmorItem {
    private static final int YELLOW = 0xD8B828;

    public HazmatItem(Type type, Settings settings) {
        super(ArmorMaterials.LEATHER, type, settings);
    }

    @Override
    public int getColor(ItemStack stack) {
        NbtCompound display = stack.getSubNbt("display");
        return display != null && display.contains("color", 99) ? display.getInt("color") : YELLOW;
    }
}
