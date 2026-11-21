package com.nexoroprime.module;

import com.nexoroprime.NexoroPrime;
import com.nexoroprime.ui.Ui;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** All built-in modules. */
public final class Modules {

    public static List<Module> create() {
        List<Module> l = new ArrayList<>();
        l.add(new FpsBoost());
        l.add(new FpsHud());
        l.add(new CoordsHud());
        l.add(new CpsHud());
        l.add(new KeystrokesHud());
        l.add(new ClockHud());
        l.add(new Zoom());
        l.add(new Fullbright());
        l.add(new ToggleSprint());
        return l;
    }

    // ---------------------------------------------------------------- HUD

    static final class FpsHud extends TextHud {
        FpsHud() { super("fps_display", "FPS Display", "Shows your frames per second.", true, 0); }
        @Override protected String text(Minecraft mc) { return NexoroPrime.modules.fps + " FPS"; }
    }

    static final class CoordsHud extends TextHud {
        CoordsHud() { super("coordinates", "Coordinates", "Shows your XYZ position and facing.", false, 0); }
        @Override protected String text(Minecraft mc) {
            if (mc.player == null) return "-";
            return String.format("%d / %d / %d  %s", (int) Math.floor(mc.player.getX()),
                    (int) Math.floor(mc.player.getY()), (int) Math.floor(mc.player.getZ()),
                    mc.player.getDirection().getName().substring(0, 1).toUpperCase());
        }
    }

    static final class CpsHud extends TextHud {
        CpsHud() { super("cps_counter", "CPS Counter", "Clicks per second (left | right).", false, 0); }
        @Override protected String text(Minecraft mc) {
            return NexoroPrime.modules.cps(true) + " | " + NexoroPrime.modules.cps(false) + " CPS";
        }
    }

    static final class ClockHud extends TextHud {
        private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");
        ClockHud() { super("clock", "Clock", "Shows your real-world time.", false, 1); }
        @Override protected String text(Minecraft mc) { return LocalTime.now().format(FMT); }
    }

    static final class KeystrokesHud extends HudModule {
        KeystrokesHud() { super("keystrokes", "Keystrokes", "WASD and mouse buttons on screen.", false, 2); }
        @Override public int width(Font f, Minecraft mc) { return 64; }
        @Override public int height() { return 60; }

        private void key(GuiGraphics g, Font f, int x, int y, int w, int h, String s, boolean down) {
            Ui.round(g, x, y, w, h, 3, down ? 0xD8FF8A1F : 0xA0101A33);
            Ui.textCenter(g, f, s, x + w / 2, y + (h - 8) / 2, down ? 0xFF1A0E02 : Ui.TEXT);
        }

        @Override
        public void render(GuiGraphics g, Font f, Minecraft mc, int x, int y) {
            Options o = mc.options;
            key(g, f, x + 22, y, 20, 20, "W", o.keyUp.isDown());
            key(g, f, x, y + 22, 20, 20, "A", o.keyLeft.isDown());
            key(g, f, x + 22, y + 22, 20, 20, "S", o.keyDown.isDown());
            key(g, f, x + 44, y + 22, 20, 20, "D", o.keyRight.isDown());
            key(g, f, x, y + 44, 31, 16, "LMB", NexoroPrime.modules.leftDown);
            key(g, f, x + 33, y + 44, 31, 16, "RMB", NexoroPrime.modules.rightDown);
        }
    }

    // ---------------------------------------------------------------- Visual / utility

    static final class Zoom extends Module {
        private final Setting fov = add(Setting.slider("fov", "Zoom FOV", 30, 70, 1, 30));
        private Integer saved;
        Zoom() { super("zoom", "Zoom", "Hold C to zoom in.", Category.VISUAL, false); }

        @Override public void onTick(Minecraft mc) {
            boolean down = NexoroPrime.keyDown(org.lwjgl.glfw.GLFW.GLFW_KEY_C) && mc.screen == null;
            if (down) {
                if (saved == null) saved = mc.options.fov().get();
                mc.options.fov().set(fov.intVal());
            } else restore(mc);
        }
        private void restore(Minecraft mc) {
            if (saved != null) { mc.options.fov().set(saved); saved = null; }
        }
        @Override public void onDisable() { restore(Minecraft.getInstance()); }
    }

    static final class Fullbright extends Module {
        private Double saved;
        Fullbright() { super("fullbright", "Fullbright", "Maximum brightness everywhere.", Category.VISUAL, false); }
        @Override public void onEnable() {
            Minecraft mc = Minecraft.getInstance();
            saved = mc.options.gamma().get();
            mc.options.gamma().set(1.0);
        }
        @Override public void onTick(Minecraft mc) {
            if (mc.options.gamma().get() < 1.0) mc.options.gamma().set(1.0);
        }
        @Override public void onDisable() {
            if (saved != null) Minecraft.getInstance().options.gamma().set(saved);
            saved = null;
        }
    }

    static final class ToggleSprint extends Module {
        ToggleSprint() { super("toggle_sprint", "Toggle Sprint", "Sprints automatically while you move forward.", Category.UTILITY, false); }
        @Override public void onTick(Minecraft mc) {
            if (mc.player != null && mc.screen == null && mc.options.keyUp.isDown()) mc.options.keySprint.setDown(true);
        }
        @Override public void onDisable() { Minecraft.getInstance().options.keySprint.setDown(false); }
    }

    // ---------------------------------------------------------------- Performance

    /**
     * FPS Boost: lowers the settings that cost the most frame time. The original values are
     * remembered (also in the config file) and restored when the module is switched off.
     */
    static final class FpsBoost extends Module {
        private final Setting level = add(Setting.slider("level", "Boost level (1 balanced - 3 extreme)", 1, 3, 1, 2));
        private final Setting unlimited = add(Setting.toggle("unlimited", "Unlimited FPS (no limit, VSync off)", true));
        private int appliedLevel = -1;

        FpsBoost() { super("fps_boost", "FPS Boost", "Lowers heavy graphics settings to raise FPS.", Category.PERFORMANCE, false); }

        @Override public void onEnable() {
            Minecraft mc = Minecraft.getInstance();
            Map<String, Double> s = com.nexoroprime.Config.data.snapshot;
            if (s.isEmpty()) {
                Options o = mc.options;
                s.put("rd", (double) o.renderDistance().get());
                s.put("sd", (double) o.simulationDistance().get());
                s.put("particles", (double) ((Enum<?>) o.particles().get()).ordinal());
                s.put("clouds", (double) o.cloudStatus().get().ordinal());
                s.put("shadows", o.entityShadows().get() ? 1.0 : 0.0);
                s.put("blend", (double) o.biomeBlendRadius().get());
                s.put("edist", o.entityDistanceScaling().get());
                s.put("fps", (double) o.framerateLimit().get());
                s.put("vsync", o.enableVsync().get() ? 1.0 : 0.0);
                s.put("bob", o.bobView().get() ? 1.0 : 0.0);
            }
            apply(mc);
        }

        @Override public void onTick(Minecraft mc) {
            if (appliedLevel != level.intVal()) apply(mc);
        }

        @Override public void onDisable() {
            Minecraft mc = Minecraft.getInstance();
            Map<String, Double> s = com.nexoroprime.Config.data.snapshot;
            if (!s.isEmpty()) {
                Options o = mc.options;
                o.renderDistance().set(s.get("rd").intValue());
                o.simulationDistance().set(s.get("sd").intValue());
                pick(() -> o.particles().get(), v -> o.particles().set(v), s.get("particles").intValue());
                o.cloudStatus().set(CloudStatus.values()[s.get("clouds").intValue()]);
                o.entityShadows().set(s.get("shadows") >= 0.5);
                o.biomeBlendRadius().set(s.get("blend").intValue());
                o.entityDistanceScaling().set(s.get("edist"));
                o.framerateLimit().set(s.get("fps").intValue());
                o.enableVsync().set(s.get("vsync") >= 0.5);
                o.bobView().set(s.get("bob") >= 0.5);
                s.clear();
            }
            appliedLevel = -1;
        }

        /** Sets an enum option by index without naming its class (works across versions). */
        private static <E extends Enum<E>> void pick(java.util.function.Supplier<E> getter,
                                                     java.util.function.Consumer<E> setter, int idx) {
            E[] all = getter.get().getDeclaringClass().getEnumConstants();
            setter.accept(all[Math.max(0, Math.min(idx, all.length - 1))]);
        }

        private void apply(Minecraft mc) {
            Map<String, Double> s = com.nexoroprime.Config.data.snapshot;
            if (s.isEmpty()) return;
            Options o = mc.options;
            int lv = level.intVal();
            appliedLevel = lv;
            int rdCap = lv == 1 ? 12 : lv == 2 ? 8 : 6;
            int sdCap = lv == 1 ? 10 : lv == 2 ? 6 : 5;
            o.renderDistance().set(Math.min(s.get("rd").intValue(), rdCap));
            o.simulationDistance().set(Math.min(s.get("sd").intValue(), sdCap));
            pick(() -> o.particles().get(), v -> o.particles().set(v), lv == 1 ? 1 : 2); // 0 all, 1 decreased, 2 minimal
            o.cloudStatus().set(lv == 1 ? CloudStatus.FAST : CloudStatus.OFF);
            o.entityShadows().set(false);
            o.biomeBlendRadius().set(lv == 1 ? 1 : 0);
            o.entityDistanceScaling().set(lv == 1 ? 0.75 : 0.5);
            o.bobView().set(lv < 3 && s.get("bob") >= 0.5);
            if (unlimited.bool()) {
                o.framerateLimit().set(260);
                o.enableVsync().set(false);
            }
        }
    }

    private Modules() {}
}
