package com.nexoroprime.module;

public enum Category {
    HUD("HUD"), VISUAL("Visual"), PERFORMANCE("Performance"), UTILITY("Utility");
    public final String label;
    Category(String label) { this.label = label; }
}
