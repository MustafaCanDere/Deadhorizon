package com.deadhorizon.entity.npc;

import com.deadhorizon.registry.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtCustomerGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.StopFollowingCustomerGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/** A bunker trader. Deals in rubles: buys scavenged goods and artifacts, sells ammo, medicine and guns. */
public class TraderEntity extends MerchantEntity {
    public TraderEntity(EntityType<? extends MerchantEntity> type, World world) {
        super(type, world);
        setPersistent();
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.GENERIC_ARMOR, 6.0);
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new StopFollowingCustomerGoal(this));
        goalSelector.add(2, new LookAtCustomerGoal(this));
        goalSelector.add(5, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        goalSelector.add(6, new LookAroundGoal(this));
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (!isAlive() || hasCustomer()) return super.interactMob(player, hand);
        if (!getWorld().isClient) {
            setCustomer(player);
            sendOffers(player, getDisplayName(), 1);
        }
        return ActionResult.success(getWorld().isClient);
    }

    private static ItemStack rubles(int n) {
        return new ItemStack(ModItems.RUBLE, n);
    }

    private static TradeOffer sell(ItemConvertible item, int count, int price) {
        return new TradeOffer(rubles(price), new ItemStack(item, count), 8, 1, 0.05f);
    }

    private static TradeOffer buy(ItemConvertible item, int count, int price) {
        return new TradeOffer(new ItemStack(item, count), rubles(price), 12, 1, 0.05f);
    }

    @Override
    protected void fillRecipes() {
        TradeOfferList offers = getOffers();
        // What he wants
        offers.add(buy(ModItems.ARTIFACT_JELLYFISH, 1, 20));
        offers.add(buy(ModItems.ARTIFACT_STONE_FLOWER, 1, 28));
        offers.add(buy(ModItems.ARTIFACT_FIREBALL, 1, 36));
        offers.add(buy(ModItems.PISTOL, 1, 8));
        offers.add(buy(ModItems.AK47, 1, 18));
        offers.add(buy(Items.GOLD_INGOT, 1, 4));
        offers.add(buy(Items.EMERALD, 1, 3));
        offers.add(buy(Items.IRON_INGOT, 4, 1));
        offers.add(buy(Items.LEATHER, 4, 1));
        // What he sells
        offers.add(sell(ModItems.AMMO_9MM, 12, 3));
        offers.add(sell(ModItems.AMMO_12G, 6, 4));
        offers.add(sell(ModItems.AMMO_556, 15, 6));
        offers.add(sell(ModItems.AMMO_762, 10, 7));
        offers.add(sell(ModItems.BANDAGE, 2, 2));
        offers.add(sell(ModItems.FIRST_AID_KIT, 1, 8));
        offers.add(sell(ModItems.ANTIBIOTICS, 1, 6));
        offers.add(sell(ModItems.ANTI_RAD, 1, 7));
        offers.add(sell(ModItems.VODKA, 1, 3));
        offers.add(sell(ModItems.CLEAN_WATER, 2, 2));
        offers.add(sell(ModItems.CANNED_BEANS, 2, 3));
        offers.add(sell(ModItems.MRE, 1, 5));
        offers.add(sell(ModItems.JERRY_CAN, 1, 6));
        offers.add(sell(ModItems.GEIGER_COUNTER, 1, 15));
        offers.add(sell(ModItems.DETECTOR, 1, 30));
        offers.add(sell(ModItems.PISTOL, 1, 20));
        offers.add(sell(ModItems.SHOTGUN, 1, 30));
        offers.add(sell(ModItems.AK47, 1, 50));
        offers.add(sell(ModItems.SUPPRESSOR, 1, 25));
        offers.add(sell(ModItems.SCOPE, 1, 25));
        offers.add(sell(ModItems.RADIO, 1, 10));
    }

    @Override
    protected void afterUsing(TradeOffer offer) {
    }

    @Override
    public boolean isLeveledMerchant() {
        return false;
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return hasCustomer() ? SoundEvents.ENTITY_VILLAGER_TRADE : SoundEvents.ENTITY_VILLAGER_AMBIENT;
    }

    @Override
    public SoundEvent getYesSound() {
        return SoundEvents.ENTITY_VILLAGER_YES;
    }

    @Override
    protected SoundEvent getTradingSound(boolean sold) {
        return sold ? SoundEvents.ENTITY_VILLAGER_YES : SoundEvents.ENTITY_VILLAGER_NO;
    }

    @Override
    public float getSoundPitch() {
        return 0.7f;
    }
}
