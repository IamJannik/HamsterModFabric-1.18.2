package net.bmjo.hamstermod.world.gen;

import net.bmjo.hamstermod.entity.ModEntities;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.mixin.object.builder.SpawnRestrictionAccessor;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;

public class ModEntitySpawn {
    public static void addEntitySpawn() {
        BiomeModifications.addSpawn(BiomeSelectors.categories(Biome.Category.PLAINS, Biome.Category.FOREST),
                SpawnGroup.AMBIENT, ModEntities.HAMSTER, 50, 1, 2);

        SpawnRestrictionAccessor.callRegister(ModEntities.HAMSTER, SpawnRestriction.Location.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);
    }
}
