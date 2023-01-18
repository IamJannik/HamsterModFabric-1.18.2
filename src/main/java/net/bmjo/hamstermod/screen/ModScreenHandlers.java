package net.bmjo.hamstermod.screen;

import net.bmjo.hamstermod.HamsterMod;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModScreenHandlers {

    public static ScreenHandlerType<HamsterWheelScreenHandler> HAMSTER_WHEEL_SCREEN_HANDLER;

    public static void registerAllScreenHandlers() {
        System.out.println("registerAllScreenHandlers");
        HAMSTER_WHEEL_SCREEN_HANDLER = Registry.register(Registry.SCREEN_HANDLER, new Identifier(HamsterMod.MOD_ID, "hamster_wheel_screen_handler"), new ScreenHandlerType<>(HamsterWheelScreenHandler::new));
    }
}
