package net.bmjo.hamstermod;

import net.bmjo.hamstermod.item.ModItems;
import net.bmjo.hamstermod.block.ModBlocks;
import net.bmjo.hamstermod.block.entity.ModBlockEntities;
import net.bmjo.hamstermod.painting.ModPaintings;
import net.bmjo.hamstermod.screen.ModScreenHandlers;
import net.bmjo.hamstermod.util.ModRegistries;
import net.bmjo.hamstermod.world.ModWorldGen;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HamsterMod implements ModInitializer {
	public static final String MOD_ID = "hamstermod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();

		ModPaintings.registerPaintings();

		ModBlockEntities.registerAllBlockEntities();

		ModWorldGen.generateModWorldGen();

		ModScreenHandlers.registerAllScreenHandlers();

		ModRegistries.registerModStuffs();
	}
}

/**
 * TODO
 *
 * Hamster Ausdauer
 * Hamster in Hamsterrad
 * Hamsterrad drehen
 *
 * Sounds
 */
