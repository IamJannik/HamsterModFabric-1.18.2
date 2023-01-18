package net.bmjo.hamstermod.item;

import net.bmjo.hamstermod.HamsterMod;
import net.bmjo.hamstermod.block.ModBlocks;
import net.bmjo.hamstermod.entity.ModEntities;
import net.bmjo.hamstermod.item.custom.HamsterWheelItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModItems {

    public static final Item HAMSTER_SPAWN_EGG = registerItem("hamster_spawn_egg", new SpawnEggItem(ModEntities.HAMSTER,
            0xf89520,0xffffff, new FabricItemSettings().group(ModItemGroup.HAMSTER)));
    public static final Item HAMSTER_WHEEL_ITEM = registerItem("hamster_wheel", new HamsterWheelItem(ModBlocks.HAMSTER_WHEEL, new FabricItemSettings().group(ModItemGroup.HAMSTER)));
    public static final Item BALL_BEARING = registerItem("ball_bearing", new Item(new FabricItemSettings().group(ModItemGroup.HAMSTER)));
    public static final Item MEALWORM = registerItem("mealworm", new Item(new FabricItemSettings().group(ModItemGroup.HAMSTER).food(ModFoodComponents.MEALWORM)));
    public static final Item FIDGET_SPINNER = registerItem("fidget_spinner", new Item(new FabricItemSettings().group(ModItemGroup.HAMSTER)));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registry.ITEM, new Identifier(HamsterMod.MOD_ID, name), item);
    }

    public static void registerModItems() {
        HamsterMod.LOGGER.info("Registering Mod Items for " + HamsterMod.MOD_ID);
    }
}
