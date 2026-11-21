package com.nexoroprime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Saves module states, settings, favorites to config/nexoroprime.json */
public final class Config {
    public static class Data {
        public Map<String, Boolean> enabled = new HashMap<>();
        public Map<String, Double> settings = new HashMap<>();
        public List<String> favorites = new ArrayList<>();
        public Map<String, Double> snapshot = new HashMap<>();
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static Data data = new Data();

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("nexoroprime.json");
    }

    public static void load() {
        try {
            Path p = file();
            if (Files.exists(p)) {
                try (Reader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
                    Data d = GSON.fromJson(r, Data.class);
                    if (d != null) {
                        data = d;
                        if (data.enabled == null) data.enabled = new HashMap<>();
                        if (data.settings == null) data.settings = new HashMap<>();
                        if (data.favorites == null) data.favorites = new ArrayList<>();
                        if (data.snapshot == null) data.snapshot = new HashMap<>();
                    }
                }
            }
        } catch (Throwable t) {
            System.err.println("[NexoroPrime] could not read config: " + t);
        }
    }

    public static void save() {
        try {
            if (NexoroPrime.modules != null) NexoroPrime.modules.collect();
            Path p = file();
            Files.createDirectories(p.getParent());
            try (Writer w = Files.newBufferedWriter(p, StandardCharsets.UTF_8)) {
                GSON.toJson(data, w);
            }
        } catch (Throwable t) {
            System.err.println("[NexoroPrime] could not save config: " + t);
        }
    }

    public static boolean isFavorite(String id) { return data.favorites.contains(id); }

    public static void toggleFavorite(String id) {
        if (!data.favorites.remove(id)) data.favorites.add(id);
        save();
    }

    private Config() {}
}
