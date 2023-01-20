package net.bmjo.hamstermod.block;

import net.bmjo.hamstermod.HamsterMod;
import net.bmjo.hamstermod.item.ModItemGroup;
import net.bmjo.hamstermod.block.custom.HamsterWheel;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModBlocks {

    public static final Block HAMSTER_WHEEL = registerBlockWithoutBlockItem("hamster_wheel",
            new HamsterWheel(FabricBlockSettings.of(Material.WOOD).strength(3f).nonOpaque()));

    public static final Block THE_CUBE = registerBlock("the_cube",
            new Block(FabricBlockSettings.of(Material.METAL).strength(6f).requiresTool()), ModItemGroup.HAMSTER);

    private static Block registerBlockWithoutBlockItem(String name, Block block) {
        return Registry.register(Registry.BLOCK, new Identifier(HamsterMod.MOD_ID, name), block);
    }
    private static Block registerBlock(String name, Block block, ItemGroup group) {
        registerBlockItem(name, block, group);
        return Registry.register(Registry.BLOCK, new Identifier(HamsterMod.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block, ItemGroup group) {
        return Registry.register(Registry.ITEM, new Identifier(HamsterMod.MOD_ID, name), new BlockItem(block, new FabricItemSettings().group(group)));
    }

    public static void registerModBlocks() {
        HamsterMod.LOGGER.info("Registering Mod Blocks for " + HamsterMod.MOD_ID);
    }
}
