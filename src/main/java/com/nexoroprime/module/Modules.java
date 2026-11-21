package com.nexoroprime.module;

import com.nexoroprime.Config;
import com.nexoroprime.NexoroPrime;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** All built-in modules (HUD widgets live in HudModules). */
public final class Modules {

    public static List<Module> create() {
        List<Module> l = new ArrayList<>();
        l.add(new FpsBoost());
        HudModules.addAll(l);
        l.add(new Zoom());
        l.add(new Fullbright());
        l.add(new NoViewBob());
        l.add(new NoFovEffects());
        l.add(new NoScreenEffects());
        l.add(new NoParticles());
        l.add(new AutoSprint());
        l.add(new AutoRespawn());
        return l;
    }

    // ---------------------------------------------------------------- Visual

    static final class Zoom extends Module {
        private final Setting fov = add(Setting.slider("fov", "Zoom FOV", 30, 70, 1, 30));
        private Integer saved;
        Zoom() { super("zoom", "Zoom", "Hold C to zoom in.", Category.VISUAL, false); }

        @Override public void onTick(Minecraft mc) {
            boolean down = NexoroPrime.keyDown(GLFW.GLFW_KEY_C) && mc.screen == null;
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

    static final class NoViewBob extends Module {
        private Boolean saved;
        NoViewBob() { super("no_view_bob", "No View Bob", "Removes the walking camera shake.", Category.VISUAL, false); }
        @Override public void onEnable() {
            Options o = Minecraft.getInstance().options;
            saved = o.bobView().get();
            o.bobView().set(false);
        }
        @Override public void onDisable() {
            if (saved != null) Minecraft.getInstance().options.bobView().set(saved);
            saved = null;
        }
    }

    static final class NoFovEffects extends Module {
        private Double saved;
        NoFovEffects() { super("no_fov_effects", "No FOV Effects", "Sprinting and speed no longer change your FOV.", Category.VISUAL, false); }
        @Override public void onEnable() {
            Options o = Minecraft.getInstance().options;
            saved = o.fovEffectScale().get();
            o.fovEffectScale().set(0.0);
        }
        @Override public void onDisable() {
            if (saved != null) Minecraft.getInstance().options.fovEffectScale().set(saved);
            saved = null;
        }
    }

    static final class NoScreenEffects extends Module {
        private Double saved;
        NoScreenEffects() { super("no_screen_effects", "No Nausea Wobble", "Removes nausea and portal screen distortion.", Category.VISUAL, false); }
        @Override public void onEnable() {
            Options o = Minecraft.getInstance().options;
            saved = o.screenEffectScale().get();
            o.screenEffectScale().set(0.0);
        }
        @Override public void onDisable() {
            if (saved != null) Minecraft.getInstance().options.screenEffectScale().set(saved);
            saved = null;
        }
    }

    static final class NoParticles extends Module {
        private Integer saved;
        NoParticles() { super("no_particles", "No Particles", "Minimal particles for cleaner visuals and more FPS.", Category.VISUAL, false); }
        @Override public void onEnable() {
            Options o = Minecraft.getInstance().options;
            saved = ((Enum<?>) o.particles().get()).ordinal();
            OptUtil.pick(() -> o.particles().get(), v -> o.particles().set(v), 2);
        }
        @Override public void onDisable() {
            if (saved == null) return;
            Options o = Minecraft.getInstance().options;
            final int idx = saved;
            OptUtil.pick(() -> o.particles().get(), v -> o.particles().set(v), idx);
            saved = null;
        }
    }

    // ---------------------------------------------------------------- Utility

    static final class AutoSprint extends Module {
        AutoSprint() { super("toggle_sprint", "Auto Sprint", "Sprints automatically while you move forward.", Category.UTILITY, false); }
        @Override public void onTick(Minecraft mc) {
            if (mc.player != null && mc.screen == null && mc.options.keyUp.isDown()) mc.options.keySprint.setDown(true);
        }
        @Override public void onDisable() { Minecraft.getInstance().options.keySprint.setDown(false); }
    }

    static final class AutoRespawn extends Module {
        AutoRespawn() { super("auto_respawn", "Auto Respawn", "Respawns you instantly when you die.", Category.UTILITY, false); }
        @Override public void onTick(Minecraft mc) {
            if (mc.player != null && mc.player.isDeadOrDying()) mc.player.respawn();
        }
    }

    // ---------------------------------------------------------------- Performance

    /**
     * FPS Boost. Remembers your original settings (also saved to the config file) and restores them
     * when switched off. Every part can be switched on/off separately.
     */
    static final class FpsBoost extends Module {
        private final Setting limRender = add(Setting.slider("render", "Limit render distance to", 4, 16, 1, 8));
        private final Setting limSim = add(Setting.slider("sim", "Limit simulation distance to", 5, 16, 1, 6));
        private final Setting entDist = add(Setting.slider("entity", "Entity distance", 0.5, 1.5, 0.25, 0.5));
        private final Setting particles = add(Setting.toggle("particles", "Minimal particles", true));
        private final Setting clouds = add(Setting.toggle("clouds", "Clouds off", true));
        private final Setting shadows = add(Setting.toggle("shadows", "Entity shadows off", true));
        private final Setting blend = add(Setting.toggle("blend", "Biome blend off", true));
        private final Setting bob = add(Setting.toggle("bob", "No view bob", false));
        private final Setting unlimited = add(Setting.toggle("unlimited", "Unlock max FPS", true));
        private final Setting vsync = add(Setting.toggle("vsync", "VSync off", true));
        private String applied = "";

        FpsBoost() { super("fps_boost", "FPS Boost", "Lowers heavy settings to raise your FPS.", Category.PERFORMANCE, false); }

        private String signature() {
            StringBuilder sb = new StringBuilder();
            for (Setting s : settings) sb.append(s.value).append(';');
            return sb.toString();
        }

        @Override public void onEnable() {
            Minecraft mc = Minecraft.getInstance();
            Map<String, Double> s = Config.data.snapshot;
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
            if (!applied.equals(signature())) apply(mc);
        }

        @Override public void onDisable() {
            Minecraft mc = Minecraft.getInstance();
            Map<String, Double> s = Config.data.snapshot;
            if (!s.isEmpty()) {
                restoreAll(mc, s);
                s.clear();
            }
            applied = "";
        }

        private void restoreAll(Minecraft mc, Map<String, Double> s) {
            Options o = mc.options;
            o.renderDistance().set(s.get("rd").intValue());
            o.simulationDistance().set(s.get("sd").intValue());
            final int pi = s.get("particles").intValue();
            OptUtil.pick(() -> o.particles().get(), v -> o.particles().set(v), pi);
            o.cloudStatus().set(CloudStatus.values()[s.get("clouds").intValue()]);
            o.entityShadows().set(s.get("shadows") >= 0.5);
            o.biomeBlendRadius().set(s.get("blend").intValue());
            o.entityDistanceScaling().set(s.get("edist"));
            o.framerateLimit().set(s.get("fps").intValue());
            o.enableVsync().set(s.get("vsync") >= 0.5);
            o.bobView().set(s.get("bob") >= 0.5);
        }

        /** Each option becomes either the boosted value or its original (snapshot) value. */
        private void apply(Minecraft mc) {
            Map<String, Double> s = Config.data.snapshot;
            if (s.isEmpty()) return;
            applied = signature();
            Options o = mc.options;
            o.renderDistance().set(Math.min(s.get("rd").intValue(), limRender.intVal()));
            o.simulationDistance().set(Math.min(s.get("sd").intValue(), limSim.intVal()));
            o.entityDistanceScaling().set(Math.min(s.get("edist"), entDist.value));
            final int pi = particles.bool() ? 2 : s.get("particles").intValue();
            OptUtil.pick(() -> o.particles().get(), v -> o.particles().set(v), pi);
            o.cloudStatus().set(clouds.bool() ? CloudStatus.OFF : CloudStatus.values()[s.get("clouds").intValue()]);
            o.entityShadows().set(shadows.bool() ? false : s.get("shadows") >= 0.5);
            o.biomeBlendRadius().set(blend.bool() ? 0 : s.get("blend").intValue());
            o.bobView().set(bob.bool() ? false : s.get("bob") >= 0.5);
            o.framerateLimit().set(unlimited.bool() ? 260 : s.get("fps").intValue());
            o.enableVsync().set(vsync.bool() ? false : s.get("vsync") >= 0.5);
        }
    }

    private Modules() {}
}
