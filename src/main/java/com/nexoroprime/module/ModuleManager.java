package com.nexoroprime.module;

import com.nexoroprime.Config;
import com.nexoroprime.ui.Ui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    public final List<Module> all = Modules.create();
    public int fps;
    public boolean leftDown, rightDown;

    private final ArrayDeque<Long> leftClicks = new ArrayDeque<>();
    private final ArrayDeque<Long> rightClicks = new ArrayDeque<>();
    private int frames;
    private long lastFpsTime = System.currentTimeMillis();

    public Module byId(String id) {
        for (Module m : all) if (m.id.equals(id)) return m;
        return null;
    }

    /** Load saved settings and enabled-states (called once, after the game options exist). */
    public void restore() {
        for (Module m : all) {
            for (Setting s : m.settings) {
                Double v = Config.data.settings.get(m.id + "." + s.key);
                if (v != null) s.set(v);
            }
        }
        for (Module m : all) {
            Boolean on = Config.data.enabled.get(m.id);
            m.setEnabled(on != null ? on : m.defaultEnabled);
        }
    }

    /** Copy live values into the config object. */
    public void collect() {
        for (Module m : all) {
            Config.data.enabled.put(m.id, m.isEnabled());
            for (Setting s : m.settings) Config.data.settings.put(m.id + "." + s.key, s.value);
        }
    }

    public void tick(Minecraft mc) {
        for (Module m : all) {
            if (!m.isEnabled()) continue;
            try { m.onTick(mc); } catch (Throwable t) {
                System.err.println("[NexoroPrime] " + m.id + " tick failed: " + t);
                m.setEnabled(false);
            }
        }
    }

    public int cps(boolean left) {
        ArrayDeque<Long> q = left ? leftClicks : rightClicks;
        long now = System.currentTimeMillis();
        while (!q.isEmpty() && now - q.peekFirst() > 1000) q.pollFirst();
        return q.size();
    }

    private void poll() {
        long win = GLFW.glfwGetCurrentContext();
        if (win == 0) return;
        boolean l = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean r = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        long now = System.currentTimeMillis();
        if (l && !leftDown) leftClicks.addLast(now);
        if (r && !rightDown) rightClicks.addLast(now);
        leftDown = l;
        rightDown = r;
    }

    public void renderHud(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        frames++;
        long now = System.currentTimeMillis();
        if (now - lastFpsTime >= 1000) {
            fps = (int) (frames * 1000L / (now - lastFpsTime));
            frames = 0;
            lastFpsTime = now;
        }
        poll();
        if (mc.options.hideGui || mc.player == null) return;

        Font f = mc.font;
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        final int margin = 6, gap = 4;
        int[] used = new int[4];
        for (Module m : all) {
            if (!m.isEnabled() || !(m instanceof HudModule h)) continue;
            int c = Math.max(0, Math.min(3, h.corner.intVal()));
            int w = h.width(f, mc), ht = h.height();
            int x = (c % 2 == 0) ? margin : sw - margin - w;
            int y = (c < 2) ? margin + used[c] : sh - margin - used[c] - ht;
            try { h.render(g, f, mc, x, y); } catch (Throwable t) { m.setEnabled(false); }
            used[c] += ht + gap;
        }
    }

    public List<Module> inCategory(Category c) {
        List<Module> out = new ArrayList<>();
        for (Module m : all) if (m.category == c) out.add(m);
        return out;
    }
}
