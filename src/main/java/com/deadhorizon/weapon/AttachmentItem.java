package com.deadhorizon.weapon;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** A gun part. Hold the gun in your off hand and use the attachment to fit it (for good). */
public class AttachmentItem extends Item {
    public enum Type {
        SUPPRESSOR("suppressor"), EXTENDED_MAG("extended_mag"), SCOPE("scope");

        private final String id;

        Type(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }

    private final Type type;

    public AttachmentItem(Settings settings, Type type) {
        super(settings);
        this.type = type;
    }

    /** Shotguns take no suppressor or scope; pistols and revolvers no scope. */
    private boolean fits(GunItem gun) {
        GunSpec spec = gun.spec();
        return switch (type) {
            case SUPPRESSOR -> spec.pellets() == 1;
            case SCOPE -> spec.pellets() == 1 && spec.range() >= 64;
            case EXTENDED_MAG -> spec.magazine() >= 10;
        };
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        ItemStack gunStack = user.getOffHandStack();
        if (hand != Hand.MAIN_HAND || !(gunStack.getItem() instanceof GunItem gun)) {
            if (!world.isClient) user.sendMessage(Text.translatable("message.deadhorizon.attach.how").formatted(Formatting.GRAY), true);
            return TypedActionResult.fail(stack);
        }
        if (!fits(gun) || GunItem.has(gunStack, type)) {
            if (!world.isClient) user.sendMessage(Text.translatable("message.deadhorizon.attach.no").formatted(Formatting.GRAY), true);
            return TypedActionResult.fail(stack);
        }
        if (!world.isClient) {
            GunItem.attach(gunStack, type);
            world.playSound(null, user.getBlockPos(), SoundEvents.ITEM_ARMOR_EQUIP_IRON, SoundCategory.PLAYERS, 1.0f, 1.3f);
        }
        if (!user.getAbilities().creativeMode) stack.decrement(1);
        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable(getTranslationKey() + ".tooltip").formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.deadhorizon.attachment").formatted(Formatting.DARK_GRAY));
    }
}
