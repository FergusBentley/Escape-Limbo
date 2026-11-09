package com.fergusb.escapelimbo.mana;

import com.fergusb.escapelimbo.EscapeLimbo;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Mana penalty for killing lots of mobs in one area */
public final class KillPenalty extends SavedData {
    private static final double[] KILLS = {0, 1, 5, 10, 15};
    private static final double[] PERCENT = {0, 1, 10, 50, 100};
    private static final double RECOVERY_PER_TICK = 3.0 / 1200;
    private static final double NEIGHBOR_SHARE = 0.5;

    private record Entry(String dimension, long chunk, double kills, long updated) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("dimension").forGetter(Entry::dimension),
                Codec.LONG.fieldOf("chunk").forGetter(Entry::chunk),
                Codec.DOUBLE.fieldOf("kills").forGetter(Entry::kills),
                Codec.LONG.fieldOf("updated").forGetter(Entry::updated)
        ).apply(instance, Entry::new));
    }

    private static final Codec<KillPenalty> CODEC = Entry.CODEC.listOf().xmap(KillPenalty::new, KillPenalty::entries);
    public static final SavedDataType<KillPenalty> TYPE = new SavedDataType<>(
            EscapeLimbo.id("kill_penalty"), KillPenalty::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private final Map<String, Map<Long, double[]>> chunks = new HashMap<>();

    private KillPenalty() {}

    private KillPenalty(List<Entry> entries) {
        for (Entry entry : entries) chunks.computeIfAbsent(entry.dimension(), d -> new HashMap<>()).put(entry.chunk(), new double[] {entry.kills(), entry.updated()});
    }

    private List<Entry> entries() {
        List<Entry> list = new ArrayList<>();
        chunks.forEach((dimension, map) -> map.forEach((chunk, value) -> list.add(new Entry(dimension, chunk, value[0], (long) value[1]))));
        return list;
    }

    public static KillPenalty get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    // 0 = no penalty, 1 = full penalty
    public double penalty(MinecraftServer server, String dimension, ChunkPos chunk) {
        return percent(currentKills(server, dimension, chunk.pack())) / 100;
    }

    public void recordKill(MinecraftServer server, String dimension, ChunkPos chunk) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                long key = ChunkPos.pack(chunk.x() + dx, chunk.z() + dz);
                double kills = currentKills(server, dimension, key) + (dx == 0 && dz == 0 ? 1 : NEIGHBOR_SHARE);
                chunks.computeIfAbsent(dimension, d -> new HashMap<>()).put(key, new double[] {kills, now(server)});
            }
        }
        setDirty();
    }

    // Get current kills value after applying recovery
    private double currentKills(MinecraftServer server, String dimension, long chunk) {
        Map<Long, double[]> map = chunks.get(dimension);
        double[] value = map == null ? null : map.get(chunk);
        if (value == null) return 0;
        double recovered = percent(value[0]) - (now(server) - value[1]) * RECOVERY_PER_TICK;
        if (recovered <= 0) {
            map.remove(chunk);
            setDirty();
            return 0;
        }
        return kills(recovered);
    }

    private static long now(MinecraftServer server) {
        return server.overworld().getGameTime();
    }

    // Get penalty from kills
    static double percent(double kills) {
        if (kills >= KILLS[KILLS.length - 1]) return 100;
        for (int i = 1; i < KILLS.length; i++) {
            if (kills <= KILLS[i]) return Mth.lerp((kills - KILLS[i - 1]) / (KILLS[i] - KILLS[i - 1]), PERCENT[i - 1], PERCENT[i]);
        }
        return 100;
    }

    // Get kills from penalty
    static double kills(double percent) {
        if (percent >= 100) return KILLS[KILLS.length - 1];
        for (int i = 1; i < PERCENT.length; i++) {
            if (percent <= PERCENT[i]) return Mth.lerp((percent - PERCENT[i - 1]) / (PERCENT[i] - PERCENT[i - 1]), KILLS[i - 1], KILLS[i]);
        }
        return KILLS[KILLS.length - 1];
    }
}