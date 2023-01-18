package net.bmjo.hamstermod.entity.client;

import com.google.common.collect.Maps;
import net.bmjo.hamstermod.HamsterMod;
import net.bmjo.hamstermod.entity.custom.HamsterEntity;
import net.bmjo.hamstermod.entity.variant.HamsterVariant;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

import java.util.Map;

public class HamsterRenderer extends GeoEntityRenderer<HamsterEntity> {

    public static final Map<HamsterVariant, Identifier> LOCATION_BY_VARIANT =
            Util.make(Maps.newEnumMap(HamsterVariant.class), (map) -> {
                map.put(HamsterVariant.DEFAULT,
                        new Identifier(HamsterMod.MOD_ID, "textures/entity/hamster/hamster.png"));
                map.put(HamsterVariant.GREY,
                        new Identifier(HamsterMod.MOD_ID, "textures/entity/hamster/hamstergrey.png"));
                map.put(HamsterVariant.WHITE,
                        new Identifier(HamsterMod.MOD_ID, "textures/entity/hamster/hamsterwhite.png"));
            });
    public HamsterRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new HamsterModel());
    }

    @Override
    public Identifier getTextureLocation(HamsterEntity entity) {
        return LOCATION_BY_VARIANT.get(entity.getVariant());
    }

    @Override
    public RenderLayer getRenderType(HamsterEntity animatable, float partialTick, MatrixStack poseStack, @Nullable VertexConsumerProvider bufferSource, @Nullable VertexConsumer buffer, int packedLight, Identifier texture) {
        if (animatable.isBaby()) {
            poseStack.scale(0.5f, 0.5f, 0.5f);
        } else {
            poseStack.scale(1f, 1f, 1f);
        }

        return super.getRenderType(animatable, partialTick, poseStack, bufferSource, buffer, packedLight, texture);
    }
}
