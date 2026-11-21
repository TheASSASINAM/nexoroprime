package com.nexoroprime.ui;

import com.nexoroprime.Config;
import com.nexoroprime.NexoroPrime;
import com.nexoroprime.module.Module;
import com.nexoroprime.module.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** The NexoroPrime client menu: sidebar + module cards + settings page. */
public class NexoroMenuScreen extends Screen {

    private enum Nav {
        HOME("Home", null), FAVORITES("Favorites", null),
        HUD("HUD", com.nexoroprime.module.Category.HUD),
        VISUAL("Visual", com.nexoroprime.module.Category.VISUAL),
        PERFORMANCE("Performance", com.nexoroprime.module.Category.PERFORMANCE),
        UTILITY("Utility", com.nexoroprime.module.Category.UTILITY);
        final String label;
        final com.nexoroprime.module.Category cat;
        Nav(String l, com.nexoroprime.module.Category c) { label = l; cat = c; }
    }

    private static final int GAP = 8, CARD_H = 58, MIN_CARD_W = 150, SIDE_W = 124;

    private Nav nav = Nav.HOME;
    private Module settingsFor;
    private double scroll;
    private boolean lastDown;
    private Setting dragging;

    public NexoroMenuScreen() { super(Component.literal("NexoroPrime")); }

    @Override public boolean isPauseScreen() { return false; }

    // keep the world un-blurred (blur costs FPS)
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, this.width, this.height, 0xB0060B18);
    }

    private List<Module> visible() {
        List<Module> out = new ArrayList<>();
        for (Module m : NexoroPrime.modules.all) {
            if (nav == Nav.HOME
                    || (nav == Nav.FAVORITES && Config.isFavorite(m.id))
                    || (nav.cat != null && m.category == nav.cat)) out.add(m);
        }
        return out;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        scroll -= sy * 20;
        if (scroll < 0) scroll = 0;
        return true;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        Minecraft mc = Minecraft.getInstance();
        Font f = mc.font;

        long win = GLFW.glfwGetCurrentContext();
        boolean down = win != 0 && GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean click = down && !lastDown;
        lastDown = down;
        if (!down && dragging != null) { dragging = null; Config.save(); }

        renderBackground(g, mx, my, pt);

        int pw = Math.min(this.width - 24, 780), ph = Math.min(this.height - 24, 440);
        int px = (this.width - pw) / 2, py = (this.height - ph) / 2;
        Ui.round(g, px - 1, py - 1, pw + 2, ph + 2, 9, 0xAAFF8A1F);
        Ui.round(g, px, py, pw, ph, 8, Ui.PANEL);

        // ---- sidebar
        Ui.round(g, px + 8, py + 8, SIDE_W, ph - 16, 6, Ui.SIDEBAR);
        Ui.big(g, f, "NEXORO", px + 18, py + 16, 1.0f, Ui.ORANGE);
        Ui.text(g, f, "PRIME", px + 18, py + 27, Ui.BLUE);
        Ui.rect(g, px + 18, py + 40, SIDE_W - 20, 1, 0x553D9BFF);
        int ny = py + 50;
        for (Nav n : Nav.values()) {
            boolean active = n == nav;
            boolean hov = Ui.inside(mx, my, px + 14, ny, SIDE_W - 12, 22);
            if (active) {
                Ui.round(g, px + 14, ny, SIDE_W - 12, 22, 5, 0x40FF8A1F);
                Ui.rect(g, px + 14, ny + 4, 2, 14, Ui.ORANGE);
            } else if (hov) {
                Ui.round(g, px + 14, ny, SIDE_W - 12, 22, 5, 0x203D9BFF);
            }
            Ui.text(g, f, n.label, px + 24, ny + 7, active ? Ui.ORANGE_SOFT : Ui.TEXT);
            if (click && hov) { nav = n; settingsFor = null; scroll = 0; }
            ny += 26;
        }
        Ui.text(g, f, NexoroPrime.modules.fps + " FPS", px + 18, py + ph - 38, Ui.MUTED);
        Ui.text(g, f, "RShift = menu", px + 18, py + ph - 26, Ui.MUTED);

        // ---- header
        int cx = px + SIDE_W + 24, cw = pw - SIDE_W - 24 - 14;
        int cy = py + 40, ch = ph - 40 - 14;
        String title = settingsFor != null ? settingsFor.name : nav.label;
        Ui.textShadow(g, f, title, cx, py + 16, Ui.TEXT);
        boolean closeHov = Ui.inside(mx, my, px + pw - 26, py + 10, 16, 16);
        Ui.round(g, px + pw - 26, py + 10, 16, 16, 5, closeHov ? 0xFFE0431F : Ui.CARD);
        Ui.textCenter(g, f, "x", px + pw - 18, py + 14, Ui.TEXT);
        if (click && closeHov) { onClose(); return; }

        if (settingsFor != null) renderSettings(g, f, mx, my, click, down, cx, cy, cw, ch);
        else renderCards(g, f, mx, my, click, cx, cy, cw, ch);
    }

    private void renderCards(GuiGraphics g, Font f, int mx, int my, boolean click, int cx, int cy, int cw, int ch) {
        List<Module> list = visible();
        if (list.isEmpty()) {
            Ui.text(g, f, nav == Nav.FAVORITES ? "No favorites yet - click the star on a module." : "Nothing here.", cx, cy + 6, Ui.MUTED);
            return;
        }
        int cols = Math.max(1, (cw + GAP) / (MIN_CARD_W + GAP));
        int cardW = (cw - (cols - 1) * GAP) / cols;
        int rows = (list.size() + cols - 1) / cols;
        double maxScroll = Math.max(0, rows * (CARD_H + GAP) - ch);
        if (scroll > maxScroll) scroll = maxScroll;

        g.enableScissor(cx, cy, cx + cw, cy + ch);
        Module clicked = null; int clickKind = 0;
        for (int i = 0; i < list.size(); i++) {
            Module m = list.get(i);
            int x = cx + (i % cols) * (cardW + GAP);
            int y = cy + (i / cols) * (CARD_H + GAP) - (int) scroll;
            if (y + CARD_H < cy || y > cy + ch) continue;
            boolean hov = Ui.inside(mx, my, x, y, cardW, CARD_H) && my >= cy && my < cy + ch;
            Ui.round(g, x, y, cardW, CARD_H, 6, hov ? Ui.CARD_HOVER : Ui.CARD);
            if (m.isEnabled()) Ui.round(g, x + 6, y + CARD_H - 3, cardW - 12, 2, 1, Ui.ORANGE);
            Ui.textShadow(g, f, Ui.trim(f, m.name, cardW - 28), x + 8, y + 7, Ui.TEXT);
            Ui.text(g, f, Ui.trim(f, m.description, cardW - 16), x + 8, y + 20, Ui.MUTED);

            boolean fav = Config.isFavorite(m.id);
            Ui.text(g, f, fav ? "\u2605" : "\u2606", x + cardW - 16, y + 6, fav ? Ui.ORANGE : Ui.MUTED);

            boolean on = m.isEnabled();
            Ui.round(g, x + 8, y + CARD_H - 21, 34, 12, 6, on ? Ui.ORANGE : Ui.OFF_PILL);
            Ui.textCenter(g, f, on ? "ON" : "OFF", x + 25, y + CARD_H - 19, on ? 0xFF1A0E02 : Ui.MUTED);
            boolean hasSet = !m.settings.isEmpty();
            if (hasSet) {
                Ui.round(g, x + cardW - 44, y + CARD_H - 21, 36, 12, 6, Ui.BLUE_DEEP);
                Ui.textCenter(g, f, "Set", x + cardW - 26, y + CARD_H - 19, Ui.TEXT);
            }
            if (click && hov) {
                clicked = m;
                if (Ui.inside(mx, my, x + cardW - 22, y + 2, 20, 16)) clickKind = 1;
                else if (hasSet && Ui.inside(mx, my, x + cardW - 44, y + CARD_H - 21, 36, 12)) clickKind = 2;
                else clickKind = 3;
            }
        }
        g.disableScissor();

        if (clicked != null) {
            if (clickKind == 1) Config.toggleFavorite(clicked.id);
            else if (clickKind == 2) { settingsFor = clicked; scroll = 0; }
            else { clicked.setEnabled(!clicked.isEnabled()); Config.save(); }
        }
    }

    private void renderSettings(GuiGraphics g, Font f, int mx, int my, boolean click, boolean down,
                                int cx, int cy, int cw, int ch) {
        Module m = settingsFor;
        // back + enable toggle
        boolean backHov = Ui.inside(mx, my, cx, cy, 52, 18);
        Ui.round(g, cx, cy, 52, 18, 5, backHov ? Ui.CARD_HOVER : Ui.CARD);
        Ui.textCenter(g, f, "< Back", cx + 26, cy + 5, Ui.TEXT);
        if (click && backHov) { settingsFor = null; return; }

        boolean on = m.isEnabled();
        boolean tHov = Ui.inside(mx, my, cx + cw - 54, cy, 54, 18);
        Ui.round(g, cx + cw - 54, cy, 54, 18, 9, on ? Ui.ORANGE : Ui.OFF_PILL);
        Ui.textCenter(g, f, on ? "ENABLED" : "DISABLED", cx + cw - 27, cy + 5, on ? 0xFF1A0E02 : Ui.MUTED);
        if (click && tHov) { m.setEnabled(!on); Config.save(); }

        Ui.text(g, f, m.description, cx, cy + 28, Ui.MUTED);

        int y = cy + 46;
        for (Setting s : m.settings) {
            Ui.round(g, cx, y, cw, 30, 6, Ui.CARD);
            Ui.text(g, f, Ui.trim(f, s.label, cw - 190), cx + 10, y + 11, Ui.TEXT);
            if (s.kind == Setting.Kind.TOGGLE) {
                boolean v = s.bool();
                int bx = cx + cw - 52, by = y + 7;
                Ui.round(g, bx, by, 40, 16, 8, v ? Ui.ORANGE : Ui.OFF_PILL);
                Ui.round(g, v ? bx + 26 : bx + 2, by + 2, 12, 12, 6, 0xFFFFFFFF);
                if (click && Ui.inside(mx, my, bx, by, 40, 16)) { s.set(v ? 0 : 1); Config.save(); }
            } else {
                int tw = 120, tx = cx + cw - tw - 52, ty = y + 14;
                Ui.round(g, tx, ty - 2, tw, 4, 2, Ui.OFF_PILL);
                double frac = (s.value - s.min) / (s.max - s.min);
                Ui.round(g, tx, ty - 2, Math.max(4, (int) (tw * frac)), 4, 2, Ui.BLUE);
                Ui.round(g, tx + (int) (tw * frac) - 4, ty - 5, 8, 10, 4, Ui.ORANGE);
                Ui.textCenter(g, f, s.display(), cx + cw - 26, y + 11, Ui.TEXT);
                if (click && Ui.inside(mx, my, tx - 4, ty - 8, tw + 8, 16)) dragging = s;
                if (dragging == s && down) s.set(s.min + Math.max(0, Math.min(1, (mx - tx) / (double) tw)) * (s.max - s.min));
            }
            y += 36;
        }
    }
}
