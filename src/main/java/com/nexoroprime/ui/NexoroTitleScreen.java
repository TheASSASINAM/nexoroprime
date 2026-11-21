package com.nexoroprime.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** NexoroPrime main menu. Lightweight (no panorama) so it costs almost no FPS. */
public class NexoroTitleScreen extends Screen {
    private boolean lastDown = true; // ignore the click that opened the screen

    public NexoroTitleScreen() { super(Component.literal("NexoroPrime")); }

    @Override public boolean shouldCloseOnEsc() { return false; }

    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        g.fillGradient(0, 0, this.width, this.height, 0xFF081532, 0xFF2B1507);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        Minecraft mc = Minecraft.getInstance();
        Font f = mc.font;
        long win = GLFW.glfwGetCurrentContext();
        boolean down = win != 0 && GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean click = down && !lastDown;
        lastDown = down;

        renderBackground(g, mx, my, pt);
        // soft glows
        Ui.round(g, -60, this.height / 2 - 40, 220, 220, 110, 0x203D9BFF);
        Ui.round(g, this.width - 170, -50, 240, 240, 120, 0x22FF8A1F);

        int cx = this.width / 2;
        int top = Math.max(20, this.height / 2 - 100);
        String a = "NEXORO", b = "PRIME";
        float s = 3.0f;
        int wa = (int) (f.width(a) * s), wb = (int) (f.width(b) * s);
        int x0 = cx - (wa + wb + 4) / 2;
        Ui.big(g, f, a, x0, top, s, Ui.ORANGE);
        Ui.big(g, f, b, x0 + wa + 4, top, s, Ui.BLUE);
        Ui.textCenter(g, f, "Minecraft Client  -  1.21.11", cx, top + 34, Ui.MUTED);

        int bw = 220, bh = 24, y = top + 56;
        if (button(g, f, mx, my, click, cx - bw / 2, y, bw, bh, "Singleplayer", true)) mc.setScreen(new SelectWorldScreen(this));
        y += bh + 8;
        if (button(g, f, mx, my, click, cx - bw / 2, y, bw, bh, "Multiplayer", false)) mc.setScreen(new JoinMultiplayerScreen(this));
        y += bh + 8;
        if (button(g, f, mx, my, click, cx - bw / 2, y, bw, bh, "Client Menu", false)) mc.setScreen(new NexoroMenuScreen());
        y += bh + 8;
        int half = (bw - 8) / 2;
        if (button(g, f, mx, my, click, cx - bw / 2, y, half, bh, "Options", false)) mc.setScreen(new OptionsScreen(this, mc.options));
        if (button(g, f, mx, my, click, cx - bw / 2 + half + 8, y, half, bh, "Quit", false)) mc.stop();

        Ui.text(g, f, "NexoroPrime 1.0.0", 6, this.height - 12, Ui.MUTED);
        Ui.text(g, f, "RShift = client menu", this.width - f.width("RShift = client menu") - 6, this.height - 12, Ui.MUTED);
    }

    private boolean button(GuiGraphics g, Font f, int mx, int my, boolean click,
                           int x, int y, int w, int h, String label, boolean primary) {
        boolean hov = Ui.inside(mx, my, x, y, w, h);
        int bg = primary ? (hov ? 0xFFFF9D45 : Ui.ORANGE) : (hov ? 0xFF2A4C9A : 0xCC16213F);
        Ui.round(g, x - 1, y - 1, w + 2, h + 2, 7, primary ? 0x55FFFFFF : 0x663D9BFF);
        Ui.round(g, x, y, w, h, 6, bg);
        Ui.textCenter(g, f, label, x + w / 2, y + (h - 8) / 2, primary ? 0xFF1A0E02 : Ui.TEXT);
        return click && hov;
    }
}
