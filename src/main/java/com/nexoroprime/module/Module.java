package com.nexoroprime.module;

import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public final String id, name, description;
    public final Category category;
    public final boolean defaultEnabled;
    public final List<Setting> settings = new ArrayList<>();
    private boolean enabled;

    protected Module(String id, String name, String description, Category category, boolean defaultEnabled) {
        this.id = id; this.name = name; this.description = description;
        this.category = category; this.defaultEnabled = defaultEnabled;
    }

    protected Setting add(Setting s) { settings.add(s); return s; }

    public Setting setting(String key) {
        for (Setting s : settings) if (s.key.equals(key)) return s;
        return null;
    }

    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean e) {
        if (e == enabled) return;
        enabled = e;
        try {
            if (e) onEnable(); else onDisable();
        } catch (Throwable t) {
            System.err.println("[NexoroPrime] module " + id + " failed: " + t);
        }
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick(Minecraft mc) {}
}
