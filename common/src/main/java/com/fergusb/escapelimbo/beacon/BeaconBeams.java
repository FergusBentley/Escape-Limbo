package com.fergusb.escapelimbo.beacon;

import com.fergusb.escapelimbo.EscapeLimbo;
import com.fergusb.escapelimbo.network.BeaconBeamsPayload;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/** Tells players once a second where the active Respawn Beacon beams in their dimension are */
public final class BeaconBeams {
    private static final int INTERVAL = 20;

    private BeaconBeams() {}

    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % INTERVAL == 0) sendNow(server);
    }

    public static void sendNow(MinecraftServer server) {
        BeaconRegistry registry = BeaconRegistry.get(server);
        long now = BeaconRegistry.now(server);
        for (ServerLevel level : server.getAllLevels()) {
            if (level.players().isEmpty()) continue;
            BeaconBeamsPayload payload = new BeaconBeamsPayload(level.dimension(), collect(registry, level.dimension(), now));
            for (ServerPlayer player : level.players()) EscapeLimbo.platform().sendToPlayer(player, payload);
        }
        registry.setDirty();
    }

    private static List<BeaconBeamsPayload.Beam> collect(BeaconRegistry registry, ResourceKey<Level> dimension, long now) {
        List<BeaconBeamsPayload.Beam> beams = new ArrayList<>();
        for (BeaconRecord record : registry.all()) {
            if (record.pos().dimension() != dimension) continue;
            record.advance(now);
            if (record.state() == RespawnBeaconState.ACTIVE) beams.add(new BeaconBeamsPayload.Beam(record.pos().pos(), record.fadeIntensity()));
        }
        return beams;
    }
}