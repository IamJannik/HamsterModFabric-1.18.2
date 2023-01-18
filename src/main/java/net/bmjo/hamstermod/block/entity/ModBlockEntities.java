package net.bmjo.hamstermod.block.entity;

import net.bmjo.hamstermod.HamsterMod;
import net.bmjo.hamstermod.block.ModBlocks;
import net.bmjo.hamstermod.block.entity.custom.HamsterWheelBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModBlockEntities {
    public static BlockEntityType<HamsterWheelBlockEntity> HAMSTER_WHEEL;

    public static void registerAllBlockEntities() {
        HAMSTER_WHEEL= Registry.register(Registry.BLOCK_ENTITY_TYPE,
                new Identifier(HamsterMod.MOD_ID, "hamster_wheel"),
                FabricBlockEntityTypeBuilder.create(HamsterWheelBlockEntity::new,
                    ModBlocks.HAMSTER_WHEEL).build(null));
    }
}
