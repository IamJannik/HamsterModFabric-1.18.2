package net.bmjo.hamstermod.block.entity.client;

import net.bmjo.hamstermod.HamsterMod;
import net.bmjo.hamstermod.block.entity.custom.HamsterWheelBlockEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class HamsterWheelModel extends AnimatedGeoModel<HamsterWheelBlockEntity> {
    @Override
    public Identifier getModelLocation(HamsterWheelBlockEntity object) {
        return new Identifier(HamsterMod.MOD_ID, "geo/hamster_wheel.geo.json");
    }

    @Override
    public Identifier getTextureLocation(HamsterWheelBlockEntity object) {
        return new Identifier(HamsterMod.MOD_ID, "textures/block/hamster_wheel.png");
    }

    @Override
    public Identifier getAnimationFileLocation(HamsterWheelBlockEntity animatable) {
        return new Identifier(HamsterMod.MOD_ID, "animations/hamster_wheel.animation.json");
    }
}
