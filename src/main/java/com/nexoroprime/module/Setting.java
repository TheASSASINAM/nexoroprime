package com.nexoroprime.module;

import java.util.Locale;

public class Setting {
    public enum Kind { TOGGLE, SLIDER }

    public final String key, label;
    public final Kind kind;
    public final double min, max, step;
    public double value;

    private Setting(String key, String label, Kind kind, double min, double max, double step, double def) {
        this.key = key; this.label = label; this.kind = kind;
        this.min = min; this.max = max; this.step = step; this.value = def;
    }

    public static Setting toggle(String key, String label, boolean def) {
        return new Setting(key, label, Kind.TOGGLE, 0, 1, 1, def ? 1 : 0);
    }

    public static Setting slider(String key, String label, double min, double max, double step, double def) {
        return new Setting(key, label, Kind.SLIDER, min, max, step, def);
    }

    public boolean bool() { return value >= 0.5; }
    public int intVal() { return (int) Math.round(value); }

    public void set(double v) {
        if (kind == Kind.TOGGLE) { value = v >= 0.5 ? 1 : 0; return; }
        if (step > 0) v = Math.round((v - min) / step) * step + min;
        value = Math.max(min, Math.min(max, v));
    }

    public String display() {
        if (kind == Kind.TOGGLE) return bool() ? "On" : "Off";
        return step >= 1 ? String.valueOf(intVal()) : String.format(Locale.ROOT, "%.2f", value);
    }
}
