package com.deadhorizon.client;

import com.deadhorizon.DeadHorizon;
import com.deadhorizon.registry.ModBlocks;
import com.deadhorizon.registry.ModEntities;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class DeadHorizonClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.WALKER,
                ctx -> new DHZombieRenderer(ctx, DeadHorizon.id("textures/entity/zombie/walker.png"), 1.0f));
        EntityRendererRegistry.register(ModEntities.RUNNER,
                ctx -> new DHZombieRenderer(ctx, DeadHorizon.id("textures/entity/zombie/runner.png"), 0.95f));
        EntityRendererRegistry.register(ModEntities.BRUTE,
                ctx -> new DHZombieRenderer(ctx, DeadHorizon.id("textures/entity/zombie/brute.png"), 1.35f));
        EntityRendererRegistry.register(ModEntities.SCREAMER,
                ctx -> new DHZombieRenderer(ctx, DeadHorizon.id("textures/entity/zombie/screamer.png"), 0.95f));
        EntityRendererRegistry.register(ModEntities.SPITTER,
                ctx -> new DHZombieRenderer(ctx, DeadHorizon.id("textures/entity/zombie/spitter.png"), 1.0f));
        EntityRendererRegistry.register(ModEntities.BLOATER,
                ctx -> new DHZombieRenderer(ctx, DeadHorizon.id("textures/entity/zombie/bloater.png"), 1.1f));
        EntityRendererRegistry.register(ModEntities.CRAWLER,
                ctx -> new DHZombieRenderer(ctx, DeadHorizon.id("textures/entity/zombie/crawler.png"), 1.0f, true));
        EntityRendererRegistry.register(ModEntities.SOLDIER,
                ctx -> new DHZombieRenderer(ctx, DeadHorizon.id("textures/entity/zombie/soldier.png"), 1.0f));
        EntityRendererRegistry.register(ModEntities.ACID_SPIT, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.GRENADE, FlyingItemEntityRenderer::new);
        EntityRendererRegistry.register(ModEntities.SEAT, EmptyEntityRenderer::new);

        ClientSurvival.register();
        SurvivalHud.register();
        FreshnessTooltip.register();
        GunClient.register();

        BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), ModBlocks.BARBED_WIRE, ModBlocks.MEDICINE_CABINET,
                ModBlocks.ELECTRIC_FENCE);
    }
}
