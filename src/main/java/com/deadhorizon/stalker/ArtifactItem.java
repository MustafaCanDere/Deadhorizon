package com.deadhorizon.stalker;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Born in anomalies. Carry it on the belt (hotbar or off hand) for its effect, and its radiation. */
public class ArtifactItem extends Item {
    public enum Kind { JELLYFISH, STONE_FLOWER, FIREBALL }

    private final Kind kind;

    public ArtifactItem(Settings settings, Kind kind) {
        super(settings);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable(getTranslationKey() + ".tooltip").formatted(Formatting.GREEN));
        tooltip.add(Text.translatable("tooltip.deadhorizon.artifact.radiation").formatted(Formatting.RED));
        tooltip.add(Text.translatable("tooltip.deadhorizon.artifact.belt").formatted(Formatting.DARK_GRAY));
    }
}
