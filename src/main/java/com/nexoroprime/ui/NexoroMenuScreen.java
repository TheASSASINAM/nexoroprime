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

/** NexoroPrime client menu: icon rail, header with search, filter pills, module card grid, settings page. */
public class NexoroMenuScreen extends Screen {

    private static final String[] CHIPS = { "ALL", "FAVORITE", "HUD", "VISUAL", "PERFORMANCE", "UTILITY" };
    private static final int GAP = 6, CARD_H = 66, RAIL = 28;
    private static final int PANEL = 0xF2101A33, CARD = 0xFF1A2547, CARD_HOV = 0xFF233260, STRIP_OFF = 0xFF2A3A66;

    private int chip = 0;
    private String query = "";
    private boolean searchFocus;
    private Module settingsFor;
    private double scroll;
    private boolean lastDown, lastRight;
    private Setting dragging;

    public NexoroMenuScreen() { super(Component.literal("NexoroPrime")); }

    @Override public boolean isPauseScreen() { return false; }

    /** Plain dark overlay: no blur call, so it can never hit the "blur only once per frame" crash. */
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        g.fillGradient(0, 0, this.width, this.height, 0xA0050A18, 0xB0100A05);
    }

    @Override public void onClose() { Config.save(); super.onClose(); }

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

    private static boolean hasSettings(Module m) {
        for (Setting s : m.settings) if (!s.hidden) return true;
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        scroll -= sy * 24;
        if (scroll < 0) scroll = 0;
        return true;
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

        int pw = Math.min(this.width - 24, 600), ph = Math.min(this.height - 24, 340);
        int px = (this.width - pw) / 2, py = (this.height - ph) / 2;
        Ui.round(g, px - 1, py - 1, pw + 2, ph + 2, 11, 0x66FF8A1F);
        Ui.round(g, px, py, pw, ph, 10, PANEL);

        // ---------------- icon rail
        Ui.round(g, px + 6, py + 6, RAIL, ph - 12, 8, Ui.SIDEBAR);
        Ui.round(g, px + 10, py + 11, 20, 20, 10, Ui.ORANGE);
        Ui.textCenter(g, f, "N", px + 20, py + 17, 0xFF1A0E02);

        int ry = py + 40;
        boolean homeHov = Ui.inside(mx, my, px + 9, ry, 22, 22);
        Ui.round(g, px + 9, ry, 22, 22, 6, settingsFor == null ? 0x55FF8A1F : homeHov ? 0x333D9BFF : 0x00000000);
        Icons.draw(g, "home", px + 14, ry + 5, 1, settingsFor == null ? Ui.ORANGE_SOFT : Ui.TEXT);
        if (click && homeHov) { settingsFor = null; scroll = 0; }

        ry += 26;
        boolean layHov = Ui.inside(mx, my, px + 9, ry, 22, 22);
        if (layHov) Ui.round(g, px + 9, ry, 22, 22, 6, 0x333D9BFF);
        Icons.draw(g, "layout", px + 14, ry + 5, 1, Ui.TEXT);
        if (click && layHov) { Config.save(); mc.setScreen(new HudEditorScreen()); return; }
        if (layHov) Ui.text(g, f, "Edit layout", px + RAIL + 12, ry + 7, Ui.MUTED);

        Ui.textCenter(g, f, String.valueOf(NexoroPrime.modules.fps), px + 20, py + ph - 24, Ui.MUTED);
        Ui.textCenter(g, f, "fps", px + 20, py + ph - 14, Ui.MUTED);

        // ---------------- header
        int cx = px + RAIL + 16, cw = pw - RAIL - 16 - 12;
        Ui.textShadow(g, f, settingsFor != null ? settingsFor.name : "Home", cx, py + 13, Ui.TEXT);

        boolean closeHov = Ui.inside(mx, my, px + pw - 22, py + 8, 14, 14);
        Ui.round(g, px + pw - 22, py + 8, 14, 14, 5, closeHov ? 0xFFE0431F : 0x40FFFFFF);
        Ui.textCenter(g, f, "x", px + pw - 15, py + 11, Ui.TEXT);
        if (click && closeHov) { onClose(); return; }

        if (settingsFor != null) { renderSettings(g, f, mx, my, click, down, cx, py + 30, cw, ph - 30 - 10); return; }

        int sw = 130, sx = px + pw - 22 - 8 - sw, sy = py + 7;
        boolean sHov = Ui.inside(mx, my, sx, sy, sw, 16);
        if (searchFocus) Ui.round(g, sx - 1, sy - 1, sw + 2, 18, 9, 0xAAFF8A1F);
        Ui.round(g, sx, sy, sw, 16, 8, CARD);
        boolean blink = (System.currentTimeMillis() / 500) % 2 == 0;
        boolean empty = query.isEmpty() && !searchFocus;
        String shown = empty ? "Search modules..." : query + (searchFocus && blink ? "_" : "");
        Ui.text(g, f, Ui.trim(f, shown, sw - 14), sx + 7, sy + 4, empty ? Ui.MUTED : Ui.TEXT);
        if (click) searchFocus = sHov;

        // ---------------- filter pills
        int chx = cx;
        for (int i = 0; i < CHIPS.length; i++) {
            int w = f.width(CHIPS[i]) + 14;
            boolean act = i == chip, hov = Ui.inside(mx, my, chx, py + 30, w, 14);
            Ui.round(g, chx, py + 30, w, 14, 7, act ? Ui.ORANGE : hov ? CARD_HOV : CARD);
            Ui.text(g, f, CHIPS[i], chx + 7, py + 33, act ? 0xFF1A0E02 : Ui.TEXT);
            if (click && hov) { chip = i; scroll = 0; }
            chx += w + 5;
        }

        renderGrid(g, f, mx, my, click, rclick, cx, py + 52, cw, ph - 52 - 24, py + ph - 17);
    }

    private void renderGrid(GuiGraphics g, Font f, int mx, int my, boolean click, boolean rclick,
                            int cx, int cy, int cw, int ch, int descY) {
        List<Module> list = visible();
        if (list.isEmpty()) {
            Ui.text(g, f, chip == 1 ? "No favorites yet - click the star on a module." : "Nothing found.", cx, cy + 6, Ui.MUTED);
            return;
        }
        int cols = Math.max(3, (cw + GAP) / (92 + GAP));
        int cardW = (cw - (cols - 1) * GAP) / cols;
        int rows = (list.size() + cols - 1) / cols;
        double maxScroll = Math.max(0, rows * (CARD_H + GAP) - GAP - ch);
        if (scroll > maxScroll) scroll = maxScroll;

        String hoverDesc = null;
        Module clicked = null; int kind = 0;
        g.enableScissor(cx - 2, cy, cx + cw + 2, cy + ch);
        for (int i = 0; i < list.size(); i++) {
            Module m = list.get(i);
            int x = cx + (i % cols) * (cardW + GAP);
            int y = cy + (i / cols) * (CARD_H + GAP) - (int) scroll;
            if (y + CARD_H < cy || y > cy + ch) continue;
            boolean hov = Ui.inside(mx, my, x, y, cardW, CARD_H) && my >= cy && my < cy + ch;
            boolean on = m.isEnabled();

            if (on) Ui.round(g, x - 1, y - 1, cardW + 2, CARD_H + 2, 8, 0xCCFF8A1F);
            Ui.round(g, x, y, cardW, CARD_H, 7, hov ? CARD_HOV : CARD);

            // icon centred in the top area
            Icons.draw(g, Icons.forModule(m.id), x + (cardW - 24) / 2, y + 9, 2, on ? Ui.ORANGE_SOFT : 0xFF9DB4F0);

            boolean hasSet = hasSettings(m);
            if (hasSet) Ui.text(g, f, "...", x + cardW - 16, y + 2, hov ? Ui.TEXT : Ui.MUTED);

            // name strip (orange when enabled), like a colour label
            Ui.round(g, x + 4, y + CARD_H - 20, cardW - 8, 16, 5, on ? Ui.ORANGE : STRIP_OFF);
            Ui.text(g, f, Ui.trim(f, m.name, cardW - 30), x + 9, y + CARD_H - 16, on ? 0xFF1A0E02 : Ui.TEXT);
            boolean fav = Config.isFavorite(m.id);
            Ui.text(g, f, fav ? "\u2605" : "\u2606", x + cardW - 16, y + CARD_H - 16, fav ? (on ? 0xFF1A0E02 : Ui.ORANGE) : (on ? 0xFF5A3410 : Ui.MUTED));

            if (hov) {
                hoverDesc = m.description;
                if (click || rclick) {
                    clicked = m;
                    if (rclick) kind = 2;
                    else if (Ui.inside(mx, my, x + cardW - 22, y + CARD_H - 20, 20, 16)) kind = 1;
                    else if (hasSet && Ui.inside(mx, my, x + cardW - 22, y, 22, 14)) kind = 2;
                    else kind = 3;
                }
            }
        }
        g.disableScissor();
        if (hoverDesc != null) Ui.text(g, f, Ui.trim(f, hoverDesc, cw), cx, descY, Ui.MUTED);

        if (clicked != null) {
            if (kind == 1) Config.toggleFavorite(clicked.id);
            else if (kind == 2) { if (hasSettings(clicked)) { settingsFor = clicked; scroll = 0; } }
            else { clicked.setEnabled(!clicked.isEnabled()); Config.save(); }
        }
    }

    private void renderSettings(GuiGraphics g, Font f, int mx, int my, boolean click, boolean down,
                                int x0, int top0, int w, int h) {
        Module m = settingsFor;
        boolean backHov = Ui.inside(mx, my, x0, top0, 52, 16);
        Ui.round(g, x0, top0, 52, 16, 8, backHov ? CARD_HOV : CARD);
        Ui.textCenter(g, f, "< Back", x0 + 26, top0 + 4, Ui.TEXT);
        if (click && backHov) { settingsFor = null; Config.save(); return; }

        boolean on = m.isEnabled();
        boolean tHov = Ui.inside(mx, my, x0 + w - 74, top0, 74, 16);
        Ui.round(g, x0 + w - 74, top0, 74, 16, 8, on ? Ui.ORANGE : STRIP_OFF);
        Ui.textCenter(g, f, on ? "ENABLED" : "DISABLED", x0 + w - 37, top0 + 4, on ? 0xFF1A0E02 : Ui.TEXT);
        if (click && tHov) { m.setEnabled(!on); Config.save(); }

        Ui.text(g, f, Ui.trim(f, m.description, w), x0, top0 + 24, Ui.MUTED);

        int top = top0 + 40, bottom = top0 + h;
        int y = top;
        g.enableScissor(x0 - 2, top, x0 + w + 2, bottom);
        for (Setting s : m.settings) {
            if (s.hidden) continue;
            Ui.round(g, x0, y, w, 32, 7, CARD);
            Ui.text(g, f, Ui.trim(f, s.label, w - 60), x0 + 9, y + 5, Ui.TEXT);
            if (s.kind == Setting.Kind.TOGGLE) {
                boolean v = s.bool();
                int bx = x0 + w - 9 - 34, by = y + 8;
                Ui.round(g, bx, by, 34, 16, 8, v ? Ui.ORANGE : STRIP_OFF);
                Ui.round(g, v ? bx + 20 : bx + 2, by + 2, 12, 12, 6, 0xFFFFFFFF);
                if (click && Ui.inside(mx, my, bx, by, 34, 16)) { s.set(v ? 0 : 1); Config.save(); }
            } else {
                int tx = x0 + 9, tw = w - 18 - 40, ty = y + 23;
                Ui.round(g, tx, ty - 2, tw, 4, 2, STRIP_OFF);
                double frac = (s.value - s.min) / (s.max - s.min);
                Ui.round(g, tx, ty - 2, Math.max(4, (int) (tw * frac)), 4, 2, Ui.BLUE);
                Ui.round(g, tx + (int) (tw * frac) - 4, ty - 5, 8, 10, 4, Ui.ORANGE);
                Ui.textCenter(g, f, s.display(), x0 + w - 9 - 16, y + 19, Ui.TEXT);
                if (click && Ui.inside(mx, my, tx - 4, ty - 8, tw + 8, 16)) dragging = s;
                if (dragging == s && down) s.set(s.min + Math.max(0, Math.min(1, (mx - tx) / (double) tw)) * (s.max - s.min));
            }
            y += 38;
        }
        g.disableScissor();
    }
}
