package net.bmjo.hamstermod.item.client;

import net.bmjo.hamstermod.item.custom.HamsterWheelItem;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class HamsterWheelItemRenderer extends GeoItemRenderer<HamsterWheelItem> {
    public HamsterWheelItemRenderer() {
        super(new HamsterWheelItemModel());
    }
}
