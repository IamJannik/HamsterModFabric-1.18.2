package net.bmjo.hamstermod.block.entity.client;

import net.bmjo.hamstermod.block.entity.custom.HamsterWheelBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

public class HamsterWheelRenderer extends GeoBlockRenderer<HamsterWheelBlockEntity> {
    public HamsterWheelRenderer(BlockEntityRendererFactory.Context context) {
        super(new HamsterWheelModel());
    }

    @Override
    public RenderLayer getRenderType(HamsterWheelBlockEntity animatable, float partialTicks, MatrixStack stack,
                                     VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder,
                                     int packedLightIn, Identifier textureLocation) {
        return RenderLayer.getEntityTranslucent(getTextureLocation(animatable));
    }
}
