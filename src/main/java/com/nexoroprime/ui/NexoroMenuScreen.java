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

/** NexoroPrime client menu: glass cards with icons, filter chips, search and a settings page. */
public class NexoroMenuScreen extends Screen {

    private static final String[] CHIPS = { "All", "HUD", "Visuals", "Performance", "Utility", "Favorites" };
    private static final int GAP = 8, CARD_H = 88, PAD = 12;

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
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) super.renderBackground(g, mx, my, pt); // blurred world, like a glass menu
        else g.fillGradient(0, 0, this.width, this.height, 0xFF081532, 0xFF2B1507);
        g.fill(0, 0, this.width, this.height, 0x66050A18);
    }

    @Override public void onClose() { Config.save(); super.onClose(); }

    private List<Module> visible() {
        List<Module> out = new ArrayList<>();
        String q = query.trim().toLowerCase();
        for (Module m : NexoroPrime.modules.all) {
            boolean ok = chip == 0
                    || (chip == 5 && Config.isFavorite(m.id))
                    || (chip >= 1 && chip <= 4 && m.category == Category.values()[chip - 1]);
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

        int pw = Math.min(this.width - 16, 348), ph = Math.min(this.height - 16, 430);
        int px = (this.width - pw) / 2, py = (this.height - ph) / 2;
        Ui.round(g, px - 1, py - 1, pw + 2, ph + 2, 11, 0x40FFFFFF);
        Ui.round(g, px, py, pw, ph, 10, 0xB00B1226);

        // ---- header: logo or module name + close button
        if (settingsFor == null) {
            float sc = 1.5f;
            int wa = (int) (f.width("NEXORO") * sc), wb = (int) (f.width("PRIME") * sc);
            int lx = px + (pw - (wa + wb + 4)) / 2;
            Ui.big(g, f, "NEXORO", lx, py + 9, sc, Ui.ORANGE);
            Ui.big(g, f, "PRIME", lx + wa + 4, py + 9, sc, Ui.BLUE);
        } else {
            Ui.textCenter(g, f, settingsFor.name, px + pw / 2, py + 13, Ui.TEXT);
        }
        boolean closeHov = Ui.inside(mx, my, px + pw - 24, py + 8, 14, 14);
        Ui.round(g, px + pw - 24, py + 8, 14, 14, 5, closeHov ? 0xFFE0431F : 0x40FFFFFF);
        Ui.textCenter(g, f, "x", px + pw - 17, py + 11, Ui.TEXT);
        if (click && closeHov) { onClose(); return; }

        if (settingsFor != null) { renderSettings(g, f, mx, my, click, down, px, py, pw, ph); return; }

        // ---- filter chips (wrap)
        int chx = px + PAD, chy = py + 34;
        for (int i = 0; i < CHIPS.length; i++) {
            int w = f.width(CHIPS[i]) + 16;
            if (chx + w > px + pw - PAD) { chx = px + PAD; chy += 20; }
            boolean act = i == chip, hov = Ui.inside(mx, my, chx, chy, w, 16);
            Ui.round(g, chx, chy, w, 16, 8, act ? Ui.ORANGE : hov ? 0x55FFFFFF : 0x30FFFFFF);
            Ui.text(g, f, CHIPS[i], chx + 8, chy + 4, act ? 0xFF1A0E02 : Ui.TEXT);
            if (click && hov) { chip = i; scroll = 0; }
            chx += w + 5;
        }
        int gridTop = chy + 16 + 8;
        int gridBottom = py + ph - 44;

        // ---- bottom bar: search + edit layout
        int by = py + ph - 24;
        int sw = pw - 2 * PAD - 92;
        boolean sHov = Ui.inside(mx, my, px + PAD, by, sw, 18);
        if (searchFocus) Ui.round(g, px + PAD - 1, by - 1, sw + 2, 20, 10, 0xAAFF8A1F);
        Ui.round(g, px + PAD, by, sw, 18, 9, 0x40FFFFFF);
        boolean blink = (System.currentTimeMillis() / 500) % 2 == 0;
        boolean empty = query.isEmpty() && !searchFocus;
        String shown = empty ? "Search modules..." : query + (searchFocus && blink ? "_" : "");
        Ui.text(g, f, Ui.trim(f, shown, sw - 16), px + PAD + 8, by + 5, empty ? Ui.MUTED : Ui.TEXT);
        if (click) searchFocus = sHov;

        int ex = px + pw - PAD - 86;
        boolean eHov = Ui.inside(mx, my, ex, by, 86, 18);
        Ui.round(g, ex, by, 86, 18, 9, eHov ? 0xFFFF9D45 : Ui.ORANGE);
        Ui.textCenter(g, f, "Edit layout", ex + 43, by + 5, 0xFF1A0E02);
        if (click && eHov) { Config.save(); mc.setScreen(new HudEditorScreen()); return; }

        renderGrid(g, f, mx, my, click, rclick, px + PAD, gridTop, pw - 2 * PAD, gridBottom - gridTop, py + ph - 38);
    }

    private void renderGrid(GuiGraphics g, Font f, int mx, int my, boolean click, boolean rclick,
                            int cx, int cy, int cw, int ch, int descY) {
        List<Module> list = visible();
        if (list.isEmpty()) {
            Ui.text(g, f, chip == 5 ? "No favorites yet - click the star on a module." : "Nothing found.", cx, cy + 6, Ui.MUTED);
            return;
        }
        int cols = 3;
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

            Ui.round(g, x - 1, y - 1, cardW + 2, CARD_H + 2, 9, on ? 0xAAFF8A1F : 0x26FFFFFF);
            Ui.round(g, x, y, cardW, CARD_H, 8, hov ? 0x4AFFFFFF : 0x30FFFFFF);

            Icons.draw(g, Icons.forModule(m.id), x + 9, y + 10, 2, on ? Ui.ORANGE : 0xFFDCE6FF);

            boolean fav = Config.isFavorite(m.id);
            Ui.text(g, f, fav ? "\u2605" : "\u2606", x + cardW - 14, y + 5, fav ? Ui.ORANGE : Ui.MUTED);
            boolean hasSet = hasSettings(m);
            if (hasSet) Ui.text(g, f, "...", x + cardW - 31, y + 4, hov ? Ui.TEXT : Ui.MUTED);

            Ui.textShadow(g, f, Ui.trim(f, m.name, cardW - 14), x + 8, y + CARD_H - 40, Ui.TEXT);
            Ui.round(g, x + 6, y + CARD_H - 25, cardW - 12, 17, 8, on ? Ui.ORANGE : 0x44FFFFFF);
            Ui.textCenter(g, f, on ? "ON" : "OFF", x + cardW / 2, y + CARD_H - 21, on ? 0xFF1A0E02 : Ui.TEXT);

            if (hov) {
                hoverDesc = m.description;
                if (click || rclick) {
                    clicked = m;
                    if (rclick) kind = 2;
                    else if (Ui.inside(mx, my, x + cardW - 20, y + 2, 18, 14)) kind = 1;
                    else if (hasSet && Ui.inside(mx, my, x + cardW - 38, y + 2, 18, 14)) kind = 2;
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
                                int px, int py, int pw, int ph) {
        Module m = settingsFor;
        int x0 = px + PAD, w = pw - 2 * PAD;

        boolean backHov = Ui.inside(mx, my, x0, py + 32, 52, 18);
        Ui.round(g, x0, py + 32, 52, 18, 9, backHov ? 0x55FFFFFF : 0x30FFFFFF);
        Ui.textCenter(g, f, "< Back", x0 + 26, py + 37, Ui.TEXT);
        if (click && backHov) { settingsFor = null; Config.save(); return; }

        boolean on = m.isEnabled();
        boolean tHov = Ui.inside(mx, my, x0 + w - 74, py + 32, 74, 18);
        Ui.round(g, x0 + w - 74, py + 32, 74, 18, 9, on ? Ui.ORANGE : 0x44FFFFFF);
        Ui.textCenter(g, f, on ? "ENABLED" : "DISABLED", x0 + w - 37, py + 37, on ? 0xFF1A0E02 : Ui.TEXT);
        if (click && tHov) { m.setEnabled(!on); Config.save(); }

        Ui.text(g, f, Ui.trim(f, m.description, w), x0, py + 58, Ui.MUTED);

        int top = py + 74, bottom = py + ph - 12;
        int y = top;
        g.enableScissor(x0 - 2, top, x0 + w + 2, bottom);
        for (Setting s : m.settings) {
            if (s.hidden) continue;
            Ui.round(g, x0, y, w, 36, 7, 0x30FFFFFF);
            Ui.text(g, f, Ui.trim(f, s.label, w - 60), x0 + 8, y + 6, Ui.TEXT);
            if (s.kind == Setting.Kind.TOGGLE) {
                boolean v = s.bool();
                int bx = x0 + w - 8 - 34, by = y + 10;
                Ui.round(g, bx, by, 34, 16, 8, v ? Ui.ORANGE : 0x55FFFFFF);
                Ui.round(g, v ? bx + 20 : bx + 2, by + 2, 12, 12, 6, 0xFFFFFFFF);
                if (click && Ui.inside(mx, my, bx, by, 34, 16)) { s.set(v ? 0 : 1); Config.save(); }
            } else {
                int tx = x0 + 8, tw = w - 16 - 40, ty = y + 26;
                Ui.round(g, tx, ty - 2, tw, 4, 2, 0x55FFFFFF);
                double frac = (s.value - s.min) / (s.max - s.min);
                Ui.round(g, tx, ty - 2, Math.max(4, (int) (tw * frac)), 4, 2, Ui.BLUE);
                Ui.round(g, tx + (int) (tw * frac) - 4, ty - 5, 8, 10, 4, Ui.ORANGE);
                Ui.textCenter(g, f, s.display(), x0 + w - 8 - 16, y + 22, Ui.TEXT);
                if (click && Ui.inside(mx, my, tx - 4, ty - 8, tw + 8, 16)) dragging = s;
                if (dragging == s && down) s.set(s.min + Math.max(0, Math.min(1, (mx - tx) / (double) tw)) * (s.max - s.min));
            }
            y += 42;
        }
        g.disableScissor();
    }
}
