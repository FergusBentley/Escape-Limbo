package com.fergusb.escapelimbo.beacon;

import com.fergusb.escapelimbo.EscapeLimbo;
import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

/** Every Respawn Beacon in the world */
public final class BeaconRegistry extends SavedData {
    private static final Codec<BeaconRegistry> CODEC = BeaconRecord.CODEC.listOf().fieldOf("beacons").codec()
            .xmap(BeaconRegistry::new, r -> List.copyOf(r.beacons.values()));

    public static final SavedDataType<BeaconRegistry> TYPE = new SavedDataType<>(
            EscapeLimbo.id("respawn_beacons"), BeaconRegistry::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private final Map<GlobalPos, BeaconRecord> beacons = new HashMap<>();

    private BeaconRegistry() {}

    private BeaconRegistry(List<BeaconRecord> records) {
        records.forEach(record -> beacons.put(record.pos(), record));
    }

    public static BeaconRegistry get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    public static long now(MinecraftServer server) {
        return server.overworld().getGameTime();
    }

    public @Nullable BeaconRecord get(GlobalPos pos) {
        return beacons.get(pos);
    }

    /** Brings a beacon up to date and returns it, or null if there's none at that position */
    public @Nullable BeaconRecord getUpdated(MinecraftServer server, GlobalPos pos) {
        BeaconRecord record = beacons.get(pos);
        if (record != null) {
            record.advance(now(server));
            setDirty();
        }
        return record;
    }

    public void put(BeaconRecord record) {
        beacons.put(record.pos(), record);
        setDirty();
    }

    public @Nullable BeaconRecord remove(GlobalPos pos) {
        BeaconRecord removed = beacons.remove(pos);
        if (removed != null) setDirty();
        return removed;
    }

    public Collection<BeaconRecord> all() {
        return beacons.values();
    }
}