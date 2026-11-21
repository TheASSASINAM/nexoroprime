package com.nexoroprime.module;

import com.nexoroprime.NexoroPrime;
import com.nexoroprime.ui.Ui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** All HUD widgets. */
public final class HudModules {

    public static void addAll(List<Module> l) {
        l.add(new FpsHud());
        l.add(new CoordsHud());
        l.add(new DirectionHud());
        l.add(new SpeedHud());
        l.add(new PingHud());
        l.add(new CpsHud());
        l.add(new KeystrokesHud());
        l.add(new ClockHud());
        l.add(new ArmorHud());
        l.add(new ItemCounterHud());
    }

    static final class FpsHud extends TextHud {
        FpsHud() { super("fps_display", "FPS Display", "Shows your frames per second.", true, 0.01, 0.02); }
        @Override protected String text(Minecraft mc) { return NexoroPrime.modules.fps + " FPS"; }
    }

    static final class CoordsHud extends TextHud {
        CoordsHud() { super("coordinates", "Coordinates", "Shows your XYZ position.", false, 0.01, 0.10); }
        @Override protected String text(Minecraft mc) {
            if (mc.player == null) return "XYZ: -";
            return String.format("XYZ: %d / %d / %d", (int) Math.floor(mc.player.getX()),
                    (int) Math.floor(mc.player.getY()), (int) Math.floor(mc.player.getZ()));
        }
    }

    static final class DirectionHud extends TextHud {
        DirectionHud() { super("direction", "Direction", "Shows which way you are facing.", false, 0.01, 0.18); }
        @Override protected String text(Minecraft mc) {
            if (mc.player == null) return "Facing: -";
            String n = mc.player.getDirection().getName();
            return "Facing: " + n.substring(0, 1).toUpperCase() + n.substring(1);
        }
    }

    static final class SpeedHud extends TextHud {
        SpeedHud() { super("speed", "Speed", "Your speed in blocks per second.", false, 0.01, 0.26); }
        @Override protected String text(Minecraft mc) {
            if (mc.player == null) return "Speed: -";
            double dx = mc.player.getDeltaMovement().x, dz = mc.player.getDeltaMovement().z;
            return String.format("Speed: %.1f b/s", Math.sqrt(dx * dx + dz * dz) * 20.0);
        }
    }

    static final class PingHud extends TextHud {
        PingHud() { super("ping", "Ping", "Your latency to the server.", false, 0.01, 0.34); }
        @Override protected String text(Minecraft mc) {
            try {
                if (mc.getConnection() != null && mc.player != null) {
                    var info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
                    if (info != null) return "Ping: " + info.getLatency() + " ms";
                }
            } catch (Throwable ignored) {}
            return "Ping: -";
        }
    }

    static final class CpsHud extends TextHud {
        CpsHud() { super("cps_counter", "CPS Counter", "Clicks per second (left | right).", false, 0.01, 0.42); }
        @Override protected String text(Minecraft mc) {
            return "CPS: " + NexoroPrime.modules.cps(true) + " | " + NexoroPrime.modules.cps(false);
        }
    }

    static final class ClockHud extends TextHud {
        private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");
        ClockHud() { super("clock", "Clock", "Your real-world time.", false, 0.99, 0.02); }
        @Override protected String text(Minecraft mc) { return "Time: " + LocalTime.now().format(FMT); }
    }

    static final class KeystrokesHud extends HudModule {
        KeystrokesHud() { super("keystrokes", "Keystrokes", "WASD and mouse buttons on screen.", false, 0.02, 0.92); }
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

    static final class ArmorHud extends HudModule {
        private final Setting dur = add(Setting.toggle("durability", "Show durability %", true));
        private static final EquipmentSlot[] SLOTS = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
        ArmorHud() { super("armor_hud", "Armor HUD", "Shows your armor and its durability.", false, 0.99, 0.55); }
        @Override public int width(Font f, Minecraft mc) { return dur.bool() ? 56 : 24; }
        @Override public int height() { return 4 * 20 + 4; }

        @Override
        public void render(GuiGraphics g, Font f, Minecraft mc, int x, int y) {
            Ui.round(g, x, y, width(f, mc), height(), 3, 0xB4101A33);
            Ui.rect(g, x, y + 2, 2, height() - 4, Ui.ORANGE);
            if (mc.player == null) return;
            for (int i = 0; i < 4; i++) {
                ItemStack st = mc.player.getItemBySlot(SLOTS[i]);
                int iy = y + 3 + i * 20;
                if (st.isEmpty()) continue;
                g.renderItem(st, x + 5, iy);
                if (dur.bool() && st.getMaxDamage() > 0) {
                    int pct = 100 - (int) Math.round(100.0 * st.getDamageValue() / st.getMaxDamage());
                    int col = pct > 50 ? 0xFF7CFF8A : pct > 20 ? 0xFFFFD25A : 0xFFFF5A5A;
                    Ui.textShadow(g, f, pct + "%", x + 26, iy + 4, col);
                }
            }
        }
    }

    static final class ItemCounterHud extends HudModule {
        private final Setting totems = add(Setting.toggle("totems", "Totems", true));
        private final Setting pearls = add(Setting.toggle("pearls", "Ender pearls", true));
        private final Setting crystals = add(Setting.toggle("crystals", "End crystals", true));
        private final Setting apples = add(Setting.toggle("apples", "Golden apples", true));
        private final Setting xp = add(Setting.toggle("xp", "XP bottles", false));
        private final Setting zero = add(Setting.toggle("zero", "Show zero", false));

        ItemCounterHud() { super("item_counter", "Item Counter", "Counts important items in your inventory.", false, 0.99, 0.30); }

        private List<Item> items() {
            List<Item> l = new ArrayList<>();
            if (totems.bool()) l.add(Items.TOTEM_OF_UNDYING);
            if (pearls.bool()) l.add(Items.ENDER_PEARL);
            if (crystals.bool()) l.add(Items.END_CRYSTAL);
            if (apples.bool()) l.add(Items.GOLDEN_APPLE);
            if (xp.bool()) l.add(Items.EXPERIENCE_BOTTLE);
            return l;
        }

        private int count(Minecraft mc, Item item) {
            int n = 0;
            if (mc.player == null) return 0;
            var inv = mc.player.getInventory();
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack st = inv.getItem(i);
                if (!st.isEmpty() && st.is(item)) n += st.getCount();
            }
            return n;
        }

        @Override public int width(Font f, Minecraft mc) { return 48; }
        @Override public int height() { return Math.max(1, items().size()) * 20 + 4; }

        @Override
        public void render(GuiGraphics g, Font f, Minecraft mc, int x, int y) {
            Ui.round(g, x, y, width(f, mc), height(), 3, 0xB4101A33);
            Ui.rect(g, x, y + 2, 2, height() - 4, Ui.ORANGE);
            int row = 0;
            for (Item it : items()) {
                int c = count(mc, it);
                if (c == 0 && !zero.bool()) { row++; continue; }
                int iy = y + 3 + row * 20;
                g.renderItem(new ItemStack(it), x + 5, iy);
                Ui.textShadow(g, f, "x" + c, x + 25, iy + 4, c == 0 ? Ui.MUTED : Ui.TEXT);
                row++;
            }
        }
    }

    private HudModules() {}
}
