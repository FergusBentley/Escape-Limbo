package com.fergusb.escapelimbo.survival;

import com.fergusb.escapelimbo.EscapeLimbo;
import com.fergusb.escapelimbo.beacon.BeaconRecord;
import com.fergusb.escapelimbo.beacon.BeaconRegistry;
import com.fergusb.escapelimbo.beacon.RespawnBeaconBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

/** Respawn beacons drain while you sleep */
public final class NightSkip {
    private NightSkip() {}

    public static void onNightSkipped(MinecraftServer server, long skipped) {
        if (skipped <= 0 || !EscapeLimbo.config().sleepDrainsBeacons) return;
        EscapeLimbo.LOGGER.info("Sleeping skipped {} ticks", skipped);
        BeaconRegistry registry = BeaconRegistry.get(server);
        long now = BeaconRegistry.now(server);
        List<BeaconRecord> records = new ArrayList<>(registry.all());
        for (BeaconRecord record : records) record.skip(now, skipped);
        registry.setDirty();
        for (BeaconRecord record : records) {
            ServerLevel level = server.getLevel(record.pos().dimension());
            if (level != null && level.isLoaded(record.pos().pos())) RespawnBeaconBlockEntity.refresh(level, record.pos().pos());
        }
    }
}