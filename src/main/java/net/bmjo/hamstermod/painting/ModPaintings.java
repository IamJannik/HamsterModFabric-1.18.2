package net.bmjo.hamstermod.painting;

import net.bmjo.hamstermod.HamsterMod;
import net.minecraft.client.tutorial.TutorialManager;
import net.minecraft.entity.decoration.painting.PaintingMotive;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModPaintings {

    public static final PaintingMotive ROYAL_HAMSTER = registerPainting("royal_hamster", new PaintingMotive(16, 16));

    private static PaintingMotive registerPainting( String name, PaintingMotive paintingMotive) {
        return Registry.register(Registry.PAINTING_MOTIVE, new Identifier(HamsterMod.MOD_ID, name), paintingMotive);
    }

    public static void registerPaintings() {
        HamsterMod.LOGGER.info("Registering Mod Paintings for " + HamsterMod.MOD_ID);
    }
}
