package net.bmjo.hamstermod.entity.client;

import net.bmjo.hamstermod.HamsterMod;
import net.bmjo.hamstermod.entity.custom.HamsterEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

public class HamsterModel extends AnimatedGeoModel<HamsterEntity> {
    @Override
    public Identifier getModelLocation(HamsterEntity object) {
        return new Identifier(HamsterMod.MOD_ID, "geo/hamster.geo.json");
    }

    @Override
    public Identifier getTextureLocation(HamsterEntity object) {
        return HamsterRenderer.LOCATION_BY_VARIANT.get(object.getVariant());
    }

    @Override
    public Identifier getAnimationFileLocation(HamsterEntity animatable) {
        return new Identifier(HamsterMod.MOD_ID, "animations/hamster.animation.json");
    }

    @Override
    public void setLivingAnimations(HamsterEntity entity, Integer uniqueID, AnimationEvent customPredicate) {
        super.setLivingAnimations(entity, uniqueID, customPredicate);
        IBone head = this.getAnimationProcessor().getBone("head");

        EntityModelData extraData = (EntityModelData) customPredicate.getExtraDataOfType(EntityModelData.class).get(0);
        if (head != null) {
            head.setRotationX(extraData.headPitch * ((float) Math.PI / 270F));
            head.setRotationY(extraData.netHeadYaw * ((float) Math.PI / 270F));
        }
    }
}
