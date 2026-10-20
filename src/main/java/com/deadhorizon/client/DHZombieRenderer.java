package com.deadhorizon.client;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ZombieEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

/** Vanilla zombie model with our own texture and an optional size multiplier. */
public class DHZombieRenderer extends ZombieEntityRenderer {
    private final Identifier texture;
    private final float scale;
    private final boolean prone;

    public DHZombieRenderer(EntityRendererFactory.Context context, Identifier texture, float scale) {
        this(context, texture, scale, false);
    }

    public DHZombieRenderer(EntityRendererFactory.Context context, Identifier texture, float scale, boolean prone) {
        super(context);
        this.texture = texture;
        this.scale = scale;
        this.prone = prone;
        this.shadowRadius *= scale;
    }

    /** Crawlers are drawn lying face down, the same way players are laid out when swimming. */
    @Override
    protected void setupTransforms(ZombieEntity entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta) {
        super.setupTransforms(entity, matrices, animationProgress, bodyYaw, tickDelta);
        if (prone && !entity.isDead()) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0f));
            matrices.translate(0.0f, -1.0f, 0.3f);
        }
    }

    @Override
    public Identifier getTexture(ZombieEntity entity) {
        return texture;
    }

    @Override
    protected void scale(ZombieEntity entity, MatrixStack matrices, float amount) {
        super.scale(entity, matrices, amount);
        matrices.scale(scale, scale, scale);
    }
}
