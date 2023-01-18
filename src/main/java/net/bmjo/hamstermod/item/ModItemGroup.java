package net.bmjo.hamstermod.item;

import net.bmjo.hamstermod.HamsterMod;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class ModItemGroup {
    public static final ItemGroup HAMSTER = FabricItemGroupBuilder.build(new Identifier(HamsterMod.MOD_ID, "hamster"), () -> new ItemStack((ModItems.BALL_BEARING)));
}
