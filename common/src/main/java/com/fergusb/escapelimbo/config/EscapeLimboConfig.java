package com.fergusb.escapelimbo.config;

import com.fergusb.escapelimbo.EscapeLimbo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** config/escape_limbo.json */
public final class EscapeLimboConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "escape_limbo.json";

    public enum StarterBeacon { PER_PLAYER, SHARED, NONE }

    public StarterBeacon starterBeacon = StarterBeacon.PER_PLAYER;
    public int startingMana = 10; // TODO
    public double passiveHungerPerTick = 0.0033;
    public double otherHungerMultiplier = 0.8;
    public int beaconDrainTicks = 24000;
    public int beaconFillPerSecond = 10;
    public int beaconFadeMana = 30;
    public boolean sleepDrainsBeacons = true;

    public static EscapeLimboConfig load(Path configDir) {
        Path file = configDir.resolve(FILE_NAME);
        EscapeLimboConfig config = new EscapeLimboConfig();
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                EscapeLimboConfig read = GSON.fromJson(reader, EscapeLimboConfig.class);
                if (read != null) config = read;
            } catch (IOException | RuntimeException e) {
                EscapeLimbo.LOGGER.error("Couldn't read {}, using defaults", file, e);
            }
        }
        config.save(file);
        return config;
    }

    private void save(Path file) {
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            EscapeLimbo.LOGGER.error("Couldn't write {}", file, e);
        }
    }
}