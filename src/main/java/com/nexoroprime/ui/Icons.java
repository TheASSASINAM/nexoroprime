package com.nexoroprime.ui;

import net.minecraft.client.gui.GuiGraphics;

import java.util.HashMap;
import java.util.Map;

/** Tiny pixel-art icons (12x12) drawn with fills - no textures needed. */
public final class Icons {
    private static final Map<String, String[]> M = new HashMap<>();

    static {
        M.put("bolt", new String[]{
                "......###...", ".....###....", "....###.....", "...###......", "..#######...", ".....###....",
                "....###.....", "...###......", "..##........", "..#........."});
        M.put("gauge", new String[]{
                "...######...", ".##......##.", "#..........#", "#.....#....#", "#....##....#", "#...##.....#",
                "#..........#", "##........##", ".##......##.", "...######..."});
        M.put("pin", new String[]{
                "....####....", "...######...", "..###..###..", "..##....##..", "..###..###..", "...######...",
                "....####....", ".....##.....", ".....##.....", "......#....."});
        M.put("compass", new String[]{
                "....####....", "..##.##.##..", ".#...##...#.", ".#..####..#.", "##.######.##", "##.######.##",
                ".#..####..#.", ".#...##...#.", "..##.##.##..", "....####...."});
        M.put("speed", new String[]{
                "..#...#.....", "...#...#....", "....#...#...", ".....#...#..", "....#...#...", "...#...#....",
                "..#...#....."});
        M.put("wifi", new String[]{
                "...######...", ".##......##.", "#..######..#", "..##....##..", ".#...##...#.", "....####....",
                ".....##.....", ".....##....."});
        M.put("cursor", new String[]{
                "#...........", "##..........", "###.........", "####........", "#####.......", "######......",
                "###.##......", "##..###.....", "#....###....", ".....###...."});
        M.put("keys", new String[]{
                "##.##.##.##.", "##.##.##.##.", "............", "##.##.##.##.", "##.##.##.##.", "............",
                "##.######.##", "##.######.##"});
        M.put("clock", new String[]{
                "...######...", ".##..##..##.", "#....##....#", "#....##....#", "#....#####.#", "#..........#",
                ".##......##.", "...######..."});
        M.put("shirt", new String[]{
                "##........##", "###..##..###", ".####..####.", "..##....##..", "..##....##..", "..########..",
                "..########..", "..########.."});
        M.put("box", new String[]{
                "############", "#..........#", "############", "#....##....#", "#....##....#", "#..........#",
                "############"});
        M.put("zoom", new String[]{
                "..#####.....", ".##...##....", "##.....##...", "#.......#...", "#.......#...", "##.....##...",
                ".##...###...", "..#####.##..", ".......###..", "........###."});
        M.put("sun", new String[]{
                ".....##.....", "#....##....#", ".#.######.#.", "..########..", "..########..", "..########..",
                ".#.######.#.", "#....##....#", ".....##....."});
        M.put("wave", new String[]{
                "..##....##..", ".#..#..#..#.", "#....##....#", "............", "..##....##..", ".#..#..#..#.",
                "#....##....#"});
        M.put("eye", new String[]{
                "...######...", ".##..##..##.", "#...####...#", ".##..##..##.", "...######..."});
        M.put("dots", new String[]{
                "#....#......", "...#....#...", "..#....#...#", "#....#......", ".....#..#...", "..#.....#...",
                "#....#....#."});
        M.put("home", new String[]{
                ".....##.....", "....####....", "...######...", "..########..", ".##########.", "..##....##..",
                "..##.##.##..", "..##.##.##..", "..##.##.##.."});
        M.put("layout", new String[]{
                "#####..#####", "#####..#####", "#####..#####", "............", "#####..#####", "#####..#####",
                "#####..#####"});
        M.put("heart", new String[]{
                "..###..###..", ".####.#####.", "############", "############", ".##########.", "..########..",
                "...######...", "....####....", ".....##....."});
    }

    public static String forModule(String id) {
        switch (id) {
            case "fps_boost": return "bolt";
            case "fps_display": return "gauge";
            case "coordinates": return "pin";
            case "direction": return "compass";
            case "speed": return "speed";
            case "toggle_sprint": return "speed";
            case "ping": return "wifi";
            case "cps_counter": return "cursor";
            case "keystrokes": return "keys";
            case "clock": return "clock";
            case "armor_hud": return "shirt";
            case "item_counter": return "box";
            case "zoom": return "zoom";
            case "fullbright": return "sun";
            case "no_view_bob": return "wave";
            case "no_fov_effects": return "eye";
            case "no_screen_effects": return "eye";
            case "no_particles": return "dots";
            case "auto_respawn": return "heart";
            default: return "box";
        }
    }

    /** Draws the icon; every source pixel becomes px x px. Merges horizontal runs into one fill. */
    public static void draw(GuiGraphics g, String icon, int x, int y, int px, int color) {
        String[] rows = M.get(icon);
        if (rows == null) rows = M.get("box");
        for (int r = 0; r < rows.length; r++) {
            String row = rows[r];
            int c = 0;
            while (c < row.length()) {
                if (row.charAt(c) != '#') { c++; continue; }
                int s = c;
                while (c < row.length() && row.charAt(c) == '#') c++;
                g.fill(x + s * px, y + r * px, x + c * px, y + (r + 1) * px, color);
            }
        }
    }

    private Icons() {}
}
