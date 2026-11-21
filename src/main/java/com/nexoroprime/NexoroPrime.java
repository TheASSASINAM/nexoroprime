package com.nexoroprime;

import com.nexoroprime.module.ModuleManager;
import com.nexoroprime.ui.NexoroMenuScreen;
import com.nexoroprime.ui.NexoroTitleScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import org.lwjgl.glfw.GLFW;

public class NexoroPrime implements ClientModInitializer {
    public static final String NAME = "NexoroPrime";
    public static ModuleManager modules;

    private static boolean restored;
    private static boolean menuKeyWasDown;

    /** Raw keyboard check (no key-mapping API needed). */
    public static boolean keyDown(int glfwKey) {
        long win = GLFW.glfwGetCurrentContext();
        return win != 0 && GLFW.glfwGetKey(win, glfwKey) == GLFW.GLFW_PRESS;
    }

    @Override
    public void onInitializeClient() {
        Config.load();
        modules = new ModuleManager();
        ClientTickEvents.END_CLIENT_TICK.register(NexoroPrime::tick);
        HudRenderCallback.EVENT.register((graphics, tickCounter) -> modules.renderHud(graphics));
    }

    private static void tick(Minecraft mc) {
        if (!restored && mc.options != null) {
            restored = true;
            modules.restore();
        }

        // Right Shift opens the client menu (in game or from the title screen)
        boolean down = keyDown(GLFW.GLFW_KEY_RIGHT_SHIFT);
        if (down && !menuKeyWasDown && (mc.screen == null || mc.screen instanceof NexoroTitleScreen)) {
            mc.setScreen(new NexoroMenuScreen());
        }
        menuKeyWasDown = down;

        // Replace the vanilla title screen with ours
        if (mc.screen != null && mc.screen.getClass() == TitleScreen.class) {
            mc.setScreen(new NexoroTitleScreen());
        }

        modules.tick(mc);
    }
}
