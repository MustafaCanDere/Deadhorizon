package com.deadhorizon.stalker;

import com.deadhorizon.item.TooltipItem;
import com.deadhorizon.survival.Survival;
import com.deadhorizon.survival.SurvivalData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** Clicks while carried (see the client); use it to read your dose and the current dose rate. */
public class GeigerItem extends TooltipItem {
    public GeigerItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient) {
            SurvivalData data = Survival.get(user);
            Formatting color = data.radiation >= 60 ? Formatting.RED : data.radiation >= 30 ? Formatting.GOLD : Formatting.GREEN;
            user.sendMessage(Text.translatable("message.deadhorizon.geiger", Math.round(data.radiation),
                    String.format("%.1f", data.exposure)).formatted(color), true);
        }
        return TypedActionResult.success(user.getStackInHand(hand), world.isClient);
    }
}
