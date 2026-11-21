package com.nexoroprime.module;

import com.nexoroprime.ui.Ui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Base for on-screen widgets. Position = one of the 4 screen corners (stacked automatically). */
public abstract class HudModule extends Module {
    public final Setting corner;

    protected HudModule(String id, String name, String desc, boolean def, int defCorner) {
        super(id, name, desc, Category.HUD, def);
        corner = add(Setting.slider("corner", "Corner (0 TL, 1 TR, 2 BL, 3 BR)", 0, 3, 1, defCorner));
    }

    public abstract int width(Font f, Minecraft mc);
    public abstract int height();
    public abstract void render(GuiGraphics g, Font f, Minecraft mc, int x, int y);

    /** Draws the standard dark pill with an orange accent. */
    protected static void panel(GuiGraphics g, int x, int y, int w, int h) {
        Ui.round(g, x, y, w, h, 3, 0xB4101A33);
        Ui.rect(g, x, y + 2, 2, h - 4, Ui.ORANGE);
    }
}
