package com.deadhorizon.weapon;

import net.minecraft.item.AxeItem;
import net.minecraft.item.ToolMaterial;

/** AxeItem's constructor is protected, hence this one-line subclass. */
public class FireAxeItem extends AxeItem {
    public FireAxeItem(ToolMaterial material, float attackDamage, float attackSpeed, Settings settings) {
        super(material, attackDamage, attackSpeed, settings);
    }
}
