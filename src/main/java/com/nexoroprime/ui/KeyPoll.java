package com.nexoroprime.ui;

import org.lwjgl.glfw.GLFW;

/** Edge-detected raw key polling (used for the search box; avoids version-specific input events). */
public final class KeyPoll {
    private static final boolean[] prev = new boolean[512];

    /** True once when the key goes down. Call at most once per key per frame. */
    public static boolean pressed(int key) {
        long w = GLFW.glfwGetCurrentContext();
        if (w == 0 || key < 0 || key >= prev.length) return false;
        boolean down = GLFW.glfwGetKey(w, key) == GLFW.GLFW_PRESS;
        boolean fire = down && !prev[key];
        prev[key] = down;
        return fire;
    }

    /** Applies typed letters/digits/space/backspace to the text. */
    public static String type(String text, int maxLen) {
        for (int k = GLFW.GLFW_KEY_A; k <= GLFW.GLFW_KEY_Z; k++)
            if (pressed(k) && text.length() < maxLen) text += (char) ('a' + (k - GLFW.GLFW_KEY_A));
        for (int k = GLFW.GLFW_KEY_0; k <= GLFW.GLFW_KEY_9; k++)
            if (pressed(k) && text.length() < maxLen) text += (char) ('0' + (k - GLFW.GLFW_KEY_0));
        if (pressed(GLFW.GLFW_KEY_SPACE) && text.length() < maxLen) text += ' ';
        if (pressed(GLFW.GLFW_KEY_BACKSPACE) && !text.isEmpty()) text = text.substring(0, text.length() - 1);
        return text;
    }

    private KeyPoll() {}
}
