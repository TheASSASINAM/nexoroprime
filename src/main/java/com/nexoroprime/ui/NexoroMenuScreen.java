package com.nexoroprime.ui;

import com.nexoroprime.Config;
import com.nexoroprime.NexoroPrime;
import com.nexoroprime.module.Category;
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

/** NexoroPrime client menu: sidebar, filter chips, search, module card grid, settings page. */
public class NexoroMenuScreen extends Screen {

    private static final String[] CHIPS = { "ALL", "FAVORITE", "HUD", "VISUAL", "PERFORMANCE", "UTILITY" };
    private static final int SIDE_W = 104, GAP = 8, CARD_H = 84, MIN_CARD_W = 104;

    private int chip = 0;
    private String query = "";
    private boolean searchFocus;
    private Module settingsFor;
    private double scroll;
    private boolean lastDown, lastRight;
    private Setting dragging;

    public NexoroMenuScreen() { super(Component.literal("NexoroPrime")); }

    @Override public boolean isPauseScreen() { return false; }

    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, this.width, this.height, 0xB0060B18);
    }

    @Override
    public void onClose() { Config.save(); super.onClose(); }

    private List<Module> visible() {
        List<Module> out = new ArrayList<>();
        String q = query.trim().toLowerCase();
        for (Module m : NexoroPrime.modules.all) {
            boolean ok = chip == 0
                    || (chip == 1 && Config.isFavorite(m.id))
                    || (chip >= 2 && m.category == Category.values()[chip - 2]);
            if (ok && !q.isEmpty() && !m.name.toLowerCase().contains(q) && !m.description.toLowerCase().contains(q)) ok = false;
            if (ok) out.add(m);
        }
        return out;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        scroll -= sy * 24;
        if (scroll < 0) scroll = 0;
        return true;
    }

    private static String monogram(String name) {
        StringBuilder sb = new StringBuilder();
        for (String w : name.split(" ")) {
            if (!w.isEmpty() && sb.length() < 2) sb.append(Character.toUpperCase(w.charAt(0)));
        }
        return sb.toString();
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        Minecraft mc = Minecraft.getInstance();
        Font f = mc.font;

        long win = GLFW.glfwGetCurrentContext();
        boolean down = win != 0 && GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rdown = win != 0 && GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        boolean click = down && !lastDown;
        boolean rclick = rdown && !lastRight;
        lastDown = down;
        lastRight = rdown;
        if (!down && dragging != null) { dragging = null; Config.save(); }
        if (searchFocus && settingsFor == null) {
            String nq = KeyPoll.type(query, 20);
            if (!nq.equals(query)) { query = nq; scroll = 0; }
        }

        renderBackground(g, mx, my, pt);

        int pw = Math.min(this.width - 20, 860), ph = Math.min(this.height - 20, 470);
        int px = (this.width - pw) / 2, py = (this.height - ph) / 2;
        Ui.round(g, px - 1, py - 1, pw + 2, ph + 2, 9, 0xAAFF8A1F);
        Ui.round(g, px, py, pw, ph, 8, Ui.PANEL);

        // ---------------- sidebar
        Ui.round(g, px + 8, py + 8, SIDE_W, ph - 16, 6, Ui.SIDEBAR);
        Ui.round(g, px + 18, py + 18, 26, 26, 13, Ui.ORANGE);
        Ui.textCenter(g, f, "N", px + 31, py + 27, 0xFF1A0E02);
        Ui.text(g, f, "NEXORO", px + 50, py + 20, Ui.ORANGE);
        Ui.text(g, f, "PRIME", px + 50, py + 32, Ui.BLUE);
        Ui.rect(g, px + 18, py + 54, SIDE_W - 20, 1, 0x553D9BFF);

        int ny = py + 64;
        boolean homeActive = settingsFor == null;
        Ui.round(g, px + 14, ny, SIDE_W - 12, 24, 5, homeActive ? 0x40FF8A1F : 0x00000000);
        if (homeActive) Ui.rect(g, px + 14, ny + 5, 2, 14, Ui.ORANGE);
        Ui.text(g, f, "Home", px + 26, ny + 8, homeActive ? Ui.ORANGE_SOFT : Ui.TEXT);
        if (click && Ui.inside(mx, my, px + 14, ny, SIDE_W - 12, 24)) { settingsFor = null; scroll = 0; }

        ny += 30;
        boolean hudHov = Ui.inside(mx, my, px + 14, ny, SIDE_W - 12, 24);
        if (hudHov) Ui.round(g, px + 14, ny, SIDE_W - 12, 24, 5, 0x203D9BFF);
        Ui.text(g, f, "Edit HUD", px + 26, ny + 8, Ui.TEXT);
        if (click && hudHov) { Config.save(); mc.setScreen(new HudEditorScreen()); return; }

        Ui.text(g, f, NexoroPrime.modules.fps + " FPS", px + 20, py + ph - 38, Ui.MUTED);
        Ui.text(g, f, "RShift = menu", px + 20, py + ph - 26, Ui.MUTED);

        // ---------------- header
        int cx = px + SIDE_W + 24, cw = pw - SIDE_W - 24 - 14;
        String title = settingsFor != null ? settingsFor.name : "Home";
        Ui.textShadow(g, f, title, cx, py + 16, Ui.TEXT);

        boolean closeHov = Ui.inside(mx, my, px + pw - 26, py + 10, 16, 16);
        Ui.round(g, px + pw - 26, py + 10, 16, 16, 5, closeHov ? 0xFFE0431F : Ui.CARD);
        Ui.textCenter(g, f, "x", px + pw - 18, py + 14, Ui.TEXT);
        if (click && closeHov) { onClose(); return; }

        if (settingsFor == null) {
            int sbx = px + pw - 26 - 8 - 150, sby = py + 10;
            boolean sHov = Ui.inside(mx, my, sbx, sby, 150, 18);
            Ui.round(g, sbx - 1, sby - 1, 152, 20, 6, searchFocus ? 0xAAFF8A1F : 0x00000000);
            Ui.round(g, sbx, sby, 150, 18, 5, Ui.CARD);
            boolean blink = (System.currentTimeMillis() / 500) % 2 == 0;
            String shown = query.isEmpty() && !searchFocus ? "Search modules..." : query + (searchFocus && blink ? "_" : "");
            Ui.text(g, f, Ui.trim(f, shown, 138), sbx + 6, sby + 5, query.isEmpty() && !searchFocus ? Ui.MUTED : Ui.TEXT);
            if (click) searchFocus = sHov;

            // chips
            int chx = cx;
            for (int i = 0; i < CHIPS.length; i++) {
                int w = f.width(CHIPS[i]) + 16;
                boolean act = i == chip, hov = Ui.inside(mx, my, chx, py + 38, w, 16);
                Ui.round(g, chx, py + 38, w, 16, 8, act ? Ui.ORANGE : hov ? Ui.CARD_HOVER : Ui.CARD);
                Ui.text(g, f, CHIPS[i], chx + 8, py + 42, act ? 0xFF1A0E02 : Ui.TEXT);
                if (click && hov) { chip = i; scroll = 0; }
                chx += w + 6;
            }
        }

        int cy = py + 64, ch = ph - 64 - 24;
        if (settingsFor != null) renderSettings(g, f, mx, my, click, down, cx, py + 36, cw, ph - 36 - 14);
        else renderGrid(g, f, mx, my, click, rclick, cx, cy, cw, ch, py + ph - 18);
    }

    private void renderGrid(GuiGraphics g, Font f, int mx, int my, boolean click, boolean rclick,
                            int cx, int cy, int cw, int ch, int descY) {
        List<Module> list = visible();
        if (list.isEmpty()) {
            Ui.text(g, f, chip == 1 ? "No favorites yet - click the star on a module." : "Nothing found.", cx, cy + 6, Ui.MUTED);
            return;
        }
        int cols = Math.max(2, (cw + GAP) / (MIN_CARD_W + GAP));
        int cardW = (cw - (cols - 1) * GAP) / cols;
        int rows = (list.size() + cols - 1) / cols;
        double maxScroll = Math.max(0, rows * (CARD_H + GAP) - ch);
        if (scroll > maxScroll) scroll = maxScroll;

        String hoverDesc = null;
        Module clicked = null; int kind = 0;
        g.enableScissor(cx, cy, cx + cw, cy + ch);
        for (int i = 0; i < list.size(); i++) {
            Module m = list.get(i);
            int x = cx + (i % cols) * (cardW + GAP);
            int y = cy + (i / cols) * (CARD_H + GAP) - (int) scroll;
            if (y + CARD_H < cy || y > cy + ch) continue;
            boolean hov = Ui.inside(mx, my, x, y, cardW, CARD_H) && my >= cy && my < cy + ch;
            boolean on = m.isEnabled();
            Ui.round(g, x, y, cardW, CARD_H, 7, hov ? Ui.CARD_HOVER : Ui.CARD);
            if (on) Ui.round(g, x, y, cardW, 2, 1, Ui.ORANGE);

            String mono = monogram(m.name);
            float sc = 2.2f;
            int tw = (int) (f.width(mono) * sc);
            Ui.big(g, f, mono, x + (cardW - tw) / 2, y + 16, sc, on ? Ui.ORANGE : 0xFF5C7CC8);

            boolean fav = Config.isFavorite(m.id);
            Ui.text(g, f, fav ? "\u2605" : "\u2606", x + cardW - 14, y + 5, fav ? Ui.ORANGE : Ui.MUTED);
            boolean hasSet = false;
            for (Setting s : m.settings) if (!s.hidden) hasSet = true;
            if (hasSet) Ui.text(g, f, "...", x + 7, y + 3, hov ? Ui.TEXT : Ui.MUTED);

            Ui.round(g, x + 5, y + CARD_H - 22, cardW - 10, 17, 5, on ? Ui.ORANGE : Ui.OFF_PILL);
            Ui.textCenter(g, f, Ui.trim(f, m.name, cardW - 18), x + cardW / 2, y + CARD_H - 18, on ? 0xFF1A0E02 : Ui.TEXT);

            if (hov) {
                hoverDesc = m.description;
                if (click || rclick) {
                    clicked = m;
                    if (rclick) kind = 2;
                    else if (Ui.inside(mx, my, x + cardW - 20, y + 2, 18, 14)) kind = 1;
                    else if (hasSet && Ui.inside(mx, my, x + 3, y + 1, 24, 14)) kind = 2;
                    else kind = 3;
                }
            }
        }
        g.disableScissor();
        if (hoverDesc != null) Ui.text(g, f, Ui.trim(f, hoverDesc, cw), cx, descY, Ui.MUTED);

        if (clicked != null) {
            if (kind == 1) Config.toggleFavorite(clicked.id);
            else if (kind == 2) {
                boolean has = false;
                for (Setting s : clicked.settings) if (!s.hidden) has = true;
                if (has) { settingsFor = clicked; scroll = 0; }
            } else { clicked.setEnabled(!clicked.isEnabled()); Config.save(); }
        }
    }

    private void renderSettings(GuiGraphics g, Font f, int mx, int my, boolean click, boolean down,
                                int cx, int cy, int cw, int ch) {
        Module m = settingsFor;
        boolean backHov = Ui.inside(mx, my, cx, cy, 52, 18);
        Ui.round(g, cx, cy, 52, 18, 5, backHov ? Ui.CARD_HOVER : Ui.CARD);
        Ui.textCenter(g, f, "< Back", cx + 26, cy + 5, Ui.TEXT);
        if (click && backHov) { settingsFor = null; Config.save(); return; }

        boolean on = m.isEnabled();
        boolean tHov = Ui.inside(mx, my, cx + cw - 70, cy, 70, 18);
        Ui.round(g, cx + cw - 70, cy, 70, 18, 9, on ? Ui.ORANGE : Ui.OFF_PILL);
        Ui.textCenter(g, f, on ? "ENABLED" : "DISABLED", cx + cw - 35, cy + 5, on ? 0xFF1A0E02 : Ui.MUTED);
        if (click && tHov) { m.setEnabled(!on); Config.save(); }

        Ui.text(g, f, Ui.trim(f, m.description, cw), cx, cy + 28, Ui.MUTED);

        int y = cy + 46;
        g.enableScissor(cx, y, cx + cw, cy + ch);
        for (Setting s : m.settings) {
            if (s.hidden) continue;
            Ui.round(g, cx, y, cw, 30, 6, Ui.CARD);
            Ui.text(g, f, Ui.trim(f, s.label, cw - 210), cx + 10, y + 11, Ui.TEXT);
            if (s.kind == Setting.Kind.TOGGLE) {
                boolean v = s.bool();
                int bx = cx + cw - 52, by = y + 7;
                Ui.round(g, bx, by, 40, 16, 8, v ? Ui.ORANGE : Ui.OFF_PILL);
                Ui.round(g, v ? bx + 26 : bx + 2, by + 2, 12, 12, 6, 0xFFFFFFFF);
                if (click && Ui.inside(mx, my, bx, by, 40, 16)) { s.set(v ? 0 : 1); Config.save(); }
            } else {
                int tw = 120, tx = cx + cw - tw - 56, ty = y + 14;
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
        g.disableScissor();
    }
}
