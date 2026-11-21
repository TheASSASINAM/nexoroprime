package com.nexoroprime.ui;

import com.nexoroprime.Config;
import com.nexoroprime.NexoroPrime;
import com.nexoroprime.module.HudModule;
import com.nexoroprime.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Drag the enabled HUD widgets anywhere on the screen. */
public class HudEditorScreen extends Screen {
    private HudModule dragging;
    private int offX, offY;
    private boolean lastDown = true;

    public HudEditorScreen() { super(Component.literal("HUD Editor")); }

    @Override public boolean isPauseScreen() { return false; }

    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, this.width, this.height, 0x70060B18);
    }

    @Override public void onClose() { Config.save(); super.onClose(); }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        Minecraft mc = Minecraft.getInstance();
        Font f = mc.font;
        long win = GLFW.glfwGetCurrentContext();
        boolean down = win != 0 && GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean click = down && !lastDown;
        lastDown = down;

        renderBackground(g, mx, my, pt);
        Ui.textCenter(g, f, "HUD Editor - drag the widgets where you want them", this.width / 2, 10, Ui.TEXT);

        boolean any = false;
        for (Module m : NexoroPrime.modules.all) {
            if (!m.isEnabled() || !(m instanceof HudModule h)) continue;
            any = true;
            int w = h.width(f, mc), ht = h.height();
            int x = h.screenX(this.width, w), y = h.screenY(this.height, ht);
            h.render(g, f, mc, x, y);
            boolean hov = Ui.inside(mx, my, x, y, w, ht);
            int oc = (hov || dragging == h) ? Ui.ORANGE : 0x883D9BFF;
            Ui.rect(g, x - 1, y - 1, w + 2, 1, oc);
            Ui.rect(g, x - 1, y + ht, w + 2, 1, oc);
            Ui.rect(g, x - 1, y, 1, ht, oc);
            Ui.rect(g, x + w, y, 1, ht, oc);
            if (click && hov && dragging == null) { dragging = h; offX = mx - x; offY = my - y; }
        }
        if (!any) Ui.textCenter(g, f, "No HUD widgets enabled. Turn some on in the menu (HUD tab).", this.width / 2, this.height / 2, Ui.MUTED);

        if (dragging != null) {
            if (down) {
                int w = dragging.width(f, mc), ht = dragging.height();
                int freeX = Math.max(1, this.width - w), freeY = Math.max(1, this.height - ht);
                int nx = Math.max(0, Math.min(freeX, mx - offX));
                int ny = Math.max(0, Math.min(freeY, my - offY));
                dragging.px.set(nx / (double) freeX);
                dragging.py.set(ny / (double) freeY);
            } else { dragging = null; Config.save(); }
        }

        int bw = 90, bx = this.width / 2 - bw / 2, by = this.height - 34;
        boolean bh = Ui.inside(mx, my, bx, by, bw, 22);
        Ui.round(g, bx, by, bw, 22, 6, bh ? 0xFFFF9D45 : Ui.ORANGE);
        Ui.textCenter(g, f, "Done", bx + bw / 2, by + 7, 0xFF1A0E02);
        if (click && bh) { Config.save(); mc.setScreen(new NexoroMenuScreen()); }
    }
}
