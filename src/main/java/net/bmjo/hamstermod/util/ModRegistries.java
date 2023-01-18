package net.bmjo.hamstermod.util;

import net.bmjo.hamstermod.entity.ModEntities;
import net.bmjo.hamstermod.entity.custom.HamsterEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public class ModRegistries {
    public static void registerModStuffs() {
        registerAttributes();
    }

    private static void registerAttributes() {
        FabricDefaultAttributeRegistry.register(ModEntities.HAMSTER, HamsterEntity.setAttributes());
    }
}
