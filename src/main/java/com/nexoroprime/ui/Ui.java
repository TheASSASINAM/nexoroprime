package com.nexoroprime.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Small drawing helpers (only uses GuiGraphics.fill / drawString). */
public final class Ui {
    public static final int ORANGE = 0xFFFF8A1F;
    public static final int ORANGE_SOFT = 0xFFFFB066;
    public static final int BLUE = 0xFF3D9BFF;
    public static final int BLUE_DEEP = 0xFF1F5FD6;
    public static final int PANEL = 0xF2101A33;
    public static final int SIDEBAR = 0xFF0B1226;
    public static final int CARD = 0xFF16213F;
    public static final int CARD_HOVER = 0xFF1F2E58;
    public static final int OFF_PILL = 0xFF2A365C;
    public static final int TEXT = 0xFFEAF1FF;
    public static final int MUTED = 0xFF8E9BBF;

    public static boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    public static void rect(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + h, c);
    }

    /** Filled rounded rectangle without overlapping fills (safe with transparency). */
    public static void round(GuiGraphics g, int x, int y, int w, int h, int r, int c) {
        r = Math.min(r, Math.min(w, h) / 2);
        if (r <= 0) { rect(g, x, y, w, h, c); return; }
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = r - (int) Math.round(Math.sqrt(r * r - dy * dy));
            g.fill(x + inset, y + i, x + w - inset, y + i + 1, c);
            g.fill(x + inset, y + h - 1 - i, x + w - inset, y + h - i, c);
        }
        g.fill(x, y + r, x + w, y + h - r, c);
    }

    public static void text(GuiGraphics g, Font f, String s, int x, int y, int c) {
        g.drawString(f, s, x, y, c, false);
    }

    public static void textShadow(GuiGraphics g, Font f, String s, int x, int y, int c) {
        g.drawString(f, s, x, y, c, true);
    }

    public static void textCenter(GuiGraphics g, Font f, String s, int cx, int y, int c) {
        g.drawString(f, s, cx - f.width(s) / 2, y, c, false);
    }

    /** Scaled text. If this fails to compile on your version, replace the body with a plain drawString. */
    public static void big(GuiGraphics g, Font f, String s, int x, int y, float scale, int c) {
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        g.drawString(f, s, 0, 0, c, true);
        g.pose().popMatrix();
    }

    public static String trim(Font f, String s, int maxW) {
        if (f.width(s) <= maxW) return s;
        while (s.length() > 1 && f.width(s + "..") > maxW) s = s.substring(0, s.length() - 1);
        return s + "..";
    }

    private Ui() {}
}
