package com.deadhorizon.client;

import com.deadhorizon.weapon.GunItem;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/** Bandits, survivors and traders: the player model with our skin; guns are shouldered in both hands. */
public class HumanRenderer<T extends MobEntity> extends BipedEntityRenderer<T, PlayerEntityModel<T>> {
    private final Identifier texture;

    public HumanRenderer(EntityRendererFactory.Context ctx, Identifier texture) {
        super(ctx, new PlayerEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER), false), 0.5f);
        this.texture = texture;
        addFeature(new ArmorFeatureRenderer<>(this,
                new BipedEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER_INNER_ARMOR)),
                new BipedEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER_OUTER_ARMOR)), ctx.getModelManager()));
    }

    @Override
    public void render(T mob, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        ItemStack main = mob.getMainHandStack();
        PlayerEntityModel<T> model = getModel();
        model.leftArmPose = BipedEntityModel.ArmPose.EMPTY;
        if (main.getItem() instanceof GunItem) {
            model.rightArmPose = mob.isAttacking() ? BipedEntityModel.ArmPose.CROSSBOW_HOLD : BipedEntityModel.ArmPose.ITEM;
        } else {
            model.rightArmPose = main.isEmpty() ? BipedEntityModel.ArmPose.EMPTY : BipedEntityModel.ArmPose.ITEM;
        }
        super.render(mob, yaw, tickDelta, matrices, vertices, light);
    }

    @Override
    public Identifier getTexture(T mob) {
        return texture;
    }
}
