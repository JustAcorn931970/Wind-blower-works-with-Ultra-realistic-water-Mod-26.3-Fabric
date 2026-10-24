package com.windblower;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

/** Persistent, range-checked settings for the Wind Blower. */
public final class WindBlowerConfig {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("windblower.properties");

    public static volatile double range = 10.0;
    public static volatile double strength = 0.24;
    public static volatile double cone = 1.10;
    public static volatile double lift = 0.018;
    public static volatile double maxSpeed = 2.35;
    public static volatile int particles = 4;

    private WindBlowerConfig() {}

    public static synchronized void load() {
        if (!Files.exists(FILE)) {
            save();
            return;
        }
        Properties p = new Properties();
        try (Reader reader = Files.newBufferedReader(FILE)) {
            p.load(reader);
            range = readDouble(p, "range", range, 2.0, 24.0);
            strength = readDouble(p, "strength", strength, 0.02, 1.50);
            cone = readDouble(p, "cone", cone, 0.20, 4.00);
            lift = readDouble(p, "lift", lift, 0.0, 0.50);
            maxSpeed = readDouble(p, "maxspeed", maxSpeed, 0.1, 6.0);
            particles = (int) readDouble(p, "particles", particles, 0, 12);
        } catch (IOException ex) {
            WindBlowerMod.LOGGER.error("Could not read Wind Blower settings from {}", FILE, ex);
        }
    }

    private static double readDouble(Properties p, String key, double fallback, double min, double max) {
        String raw = p.getProperty(key);
        if (raw == null) return fallback;
        try {
            double parsed = Double.parseDouble(raw.trim());
            return Double.isFinite(parsed) && parsed >= min && parsed <= max ? parsed : fallback;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    public static synchronized boolean set(String key, double value) {
        if (!Double.isFinite(value)) return false;
        String normalized = key.toLowerCase(Locale.ROOT).replace('-', '_');
        switch (normalized) {
            case "range" -> {
                if (value < 2.0 || value > 24.0) return false;
                range = value;
            }
            case "strength" -> {
                if (value < 0.02 || value > 1.50) return false;
                strength = value;
            }
            case "cone", "width" -> {
                if (value < 0.20 || value > 4.00) return false;
                cone = value;
            }
            case "lift" -> {
                if (value < 0.0 || value > 0.50) return false;
                lift = value;
            }
            case "maxspeed", "max_speed" -> {
                if (value < 0.1 || value > 6.0) return false;
                maxSpeed = value;
            }
            case "particles" -> {
                if (value < 0.0 || value > 12.0 || value != Math.rint(value)) return false;
                particles = (int) value;
            }
            default -> { return false; }
        }
        save();
        return true;
    }

    public static synchronized void reset() {
        range = 10.0;
        strength = 0.24;
        cone = 1.10;
        lift = 0.018;
        maxSpeed = 2.35;
        particles = 4;
        save();
    }

    public static synchronized void save() {
        Properties p = new Properties();
        p.setProperty("range", Double.toString(range));
        p.setProperty("strength", Double.toString(strength));
        p.setProperty("cone", Double.toString(cone));
        p.setProperty("lift", Double.toString(lift));
        p.setProperty("maxspeed", Double.toString(maxSpeed));
        p.setProperty("particles", Integer.toString(particles));
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE)) {
                p.store(writer, "Wind Blower settings. Values can also be changed with /windblow set.");
            }
        } catch (IOException ex) {
            WindBlowerMod.LOGGER.error("Could not save Wind Blower settings to {}", FILE, ex);
        }
    }

    public static String valueFor(String key) {
        return switch (key.toLowerCase(Locale.ROOT).replace('-', '_')) {
            case "range" -> Double.toString(range);
            case "strength" -> Double.toString(strength);
            case "cone", "width" -> Double.toString(cone);
            case "lift" -> Double.toString(lift);
            case "maxspeed", "max_speed" -> Double.toString(maxSpeed);
            case "particles" -> Integer.toString(particles);
            default -> "unknown";
        };
    }
}
