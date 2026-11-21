package com.nexoroprime.module;

import com.nexoroprime.ui.Ui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** On-screen widget. Position is stored as a fraction of the free screen space (px, py) and moved in the HUD editor. */
public abstract class HudModule extends Module {
    public final Setting px, py;

    protected HudModule(String id, String name, String desc, boolean def, double x, double y) {
        super(id, name, desc, Category.HUD, def);
        px = add(Setting.slider("px", "X", 0, 1, 0.001, x).hide());
        py = add(Setting.slider("py", "Y", 0, 1, 0.001, y).hide());
    }

    public abstract int width(Font f, Minecraft mc);
    public abstract int height();
    public abstract void render(GuiGraphics g, Font f, Minecraft mc, int x, int y);

    public int screenX(int sw, int w) { return (int) Math.round(px.value * Math.max(0, sw - w)); }
    public int screenY(int sh, int h) { return (int) Math.round(py.value * Math.max(0, sh - h)); }

    /** Standard dark pill with an orange accent. */
    protected static void panel(GuiGraphics g, int x, int y, int w, int h) {
        Ui.round(g, x, y, w, h, 3, 0xB4101A33);
        Ui.rect(g, x, y + 2, 2, h - 4, Ui.ORANGE);
    }
}
