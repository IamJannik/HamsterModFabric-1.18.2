package net.bmjo.hamstermod.item.client;

import net.bmjo.hamstermod.HamsterMod;
import net.bmjo.hamstermod.item.custom.HamsterWheelItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class HamsterWheelItemModel extends AnimatedGeoModel<HamsterWheelItem> {
    @Override
    public Identifier getModelLocation(HamsterWheelItem object) {
        return new Identifier(HamsterMod.MOD_ID, "geo/hamster_wheel.geo.json");
    }

    @Override
    public Identifier getTextureLocation(HamsterWheelItem object) {
        return new Identifier(HamsterMod.MOD_ID, "textures/block/hamster_wheel.png");
    }

    @Override
    public Identifier getAnimationFileLocation(HamsterWheelItem animatable) {
        return new Identifier(HamsterMod.MOD_ID, "animations/hamster_wheel.animation.json");
    }
}
