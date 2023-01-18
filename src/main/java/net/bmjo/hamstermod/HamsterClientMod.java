package net.bmjo.hamstermod;

import net.bmjo.hamstermod.block.ModBlocks;
import net.bmjo.hamstermod.block.entity.ModBlockEntities;
import net.bmjo.hamstermod.block.entity.client.HamsterWheelRenderer;
import net.bmjo.hamstermod.entity.ModEntities;
import net.bmjo.hamstermod.entity.client.HamsterRenderer;
import net.bmjo.hamstermod.item.ModItems;
import net.bmjo.hamstermod.item.client.HamsterWheelItemRenderer;
import net.bmjo.hamstermod.screen.HamsterWheelScreen;
import net.bmjo.hamstermod.screen.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class HamsterClientMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.HAMSTER_WHEEL, RenderLayer.getCutout());

        HandledScreens.register(ModScreenHandlers.HAMSTER_WHEEL_SCREEN_HANDLER, HamsterWheelScreen::new);

        EntityRendererRegistry.register(ModEntities.HAMSTER, HamsterRenderer::new);

        GeoItemRenderer.registerItemRenderer(ModItems.HAMSTER_WHEEL_ITEM, new HamsterWheelItemRenderer());
        BlockEntityRendererRegistry.register(ModBlockEntities.HAMSTER_WHEEL, HamsterWheelRenderer::new);
    }
}
