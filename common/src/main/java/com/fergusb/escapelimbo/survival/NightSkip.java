package com.fergusb.escapelimbo.survival;

import com.fergusb.escapelimbo.EscapeLimbo;
import net.minecraft.server.MinecraftServer;

/** Respawn beacons drain while you sleep */
public final class NightSkip {
    private NightSkip() {}

    // TODO
    public static void onNightSkipped(MinecraftServer server, long skipped) {
        if (skipped <= 0) return;
        EscapeLimbo.LOGGER.info("Sleeping skipped {} ticks", skipped);
    }
}