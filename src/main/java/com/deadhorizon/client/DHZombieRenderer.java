package com.deadhorizon.client;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ZombieEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Identifier;

/** Vanilla zombie model with our own texture and an optional size multiplier. */
public class DHZombieRenderer extends ZombieEntityRenderer {
    private final Identifier texture;
    private final float scale;

    public DHZombieRenderer(EntityRendererFactory.Context context, Identifier texture, float scale) {
        super(context);
        this.texture = texture;
        this.scale = scale;
        this.shadowRadius *= scale;
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
