package com.nexoroprime.module;

import com.nexoroprime.ui.Ui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public abstract class TextHud extends HudModule {
    protected TextHud(String id, String name, String desc, boolean def, int corner) {
        super(id, name, desc, def, corner);
    }

    protected abstract String text(Minecraft mc);

    @Override public int width(Font f, Minecraft mc) { return f.width(text(mc)) + 14; }
    @Override public int height() { return 16; }

    @Override
    public void render(GuiGraphics g, Font f, Minecraft mc, int x, int y) {
        panel(g, x, y, width(f, mc), height());
        Ui.textShadow(g, f, text(mc), x + 7, y + 4, Ui.TEXT);
    }
}
