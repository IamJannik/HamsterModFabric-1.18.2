package net.bmjo.hamstermod.entity;

import net.bmjo.hamstermod.HamsterMod;
import net.bmjo.hamstermod.entity.custom.HamsterEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModEntities {
    public static final EntityType<HamsterEntity> HAMSTER = Registry.register(Registry.ENTITY_TYPE, new Identifier(HamsterMod.MOD_ID, "hamster"), FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, HamsterEntity::new)
            .dimensions(EntityDimensions.fixed(0.4f, 0.3f)).build());
}
