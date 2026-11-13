package com.fergusb.escapelimbo.beacon;

import com.fergusb.escapelimbo.EscapeLimbo;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.Mth;

public final class BeaconRecord {
    public static final int BASE_CAPACITY = 100;
    public static final int FILL_LEVELS = 16;

    public static final Codec<BeaconRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.fieldOf("pos").forGetter(r -> r.pos),
            Codec.DOUBLE.fieldOf("pool").forGetter(r -> r.pool),
            RespawnBeaconState.CODEC.fieldOf("state").forGetter(r -> r.state),
            Codec.BOOL.fieldOf("sky_clear").forGetter(r -> r.skyClear),
            Codec.LONG.fieldOf("last_tick").forGetter(r -> r.lastTick),
            UUIDUtil.CODEC.optionalFieldOf("placer").forGetter(r -> r.placer)
    ).apply(instance, BeaconRecord::new));

    private final GlobalPos pos;
    private double pool;
    private RespawnBeaconState state;
    private boolean skyClear;
    private long lastTick;
    private final Optional<UUID> placer;

    private BeaconRecord(GlobalPos pos, double pool, RespawnBeaconState state, boolean skyClear, long lastTick, Optional<UUID> placer) {
        this.pos = pos;
        this.pool = pool;
        this.state = state;
        this.skyClear = skyClear;
        this.lastTick = lastTick;
        this.placer = placer;
    }

    public static BeaconRecord placed(GlobalPos pos, long now, Optional<UUID> placer) {
        return new BeaconRecord(pos, 0, RespawnBeaconState.UNPRIMED, true, now, placer);
    }

    private static double drainPerTick() {
        return (double) BASE_CAPACITY / EscapeLimbo.config().beaconDrainTicks;
    }

    /** Catches up on draining since the last update */
    public void advance(long now) {
        long elapsed = now - lastTick;
        lastTick = now;
        if (elapsed <= 0) return;
        if (state == RespawnBeaconState.ACTIVE) {
            pool -= elapsed * drainPerTick();
            if (pool <= 0) {
                pool = 0;
                state = RespawnBeaconState.EMPTY;
            }
        }
        checkActivation();
    }

    /** Catches up to now, then adds 'ticks' more */
    public void skip(long now, long ticks) {
        advance(now);
        lastTick = now - ticks;
        advance(now);
    }

    /** Reaching 100 turns an unprimed or empty beacon on */
    private void checkActivation() {
        if ((state == RespawnBeaconState.UNPRIMED || state == RespawnBeaconState.EMPTY) && pool >= BASE_CAPACITY) {
            state = skyClear ? RespawnBeaconState.ACTIVE : RespawnBeaconState.OBSTRUCTED;
        }
    }

    public void setSkyClear(boolean clear) {
        skyClear = clear;
        if (state == RespawnBeaconState.ACTIVE && !clear) {
            state = RespawnBeaconState.OBSTRUCTED;
        } else if (state == RespawnBeaconState.OBSTRUCTED && clear) {
            state = pool > 0 ? RespawnBeaconState.ACTIVE : RespawnBeaconState.EMPTY;
        }
    }

    public double addMana(double amount) {
        double accepted = Math.max(0, Math.min(amount, capacity() - pool));
        pool += accepted;
        checkActivation();
        return accepted;
    }

    public int capacity() {
        return BASE_CAPACITY;
    }

    public int fillLevel() {
        if (pool <= 0) return 0;
        return Mth.clamp((int) Math.ceil(pool / capacity() * FILL_LEVELS), 1, FILL_LEVELS);
    }

    /** The light starts fading below this much mana */
    private static double flickerThreshold() {
        return Math.max(6, EscapeLimbo.config().beaconFadeMana);
    }

    public int lightLevel() {
        if (state != RespawnBeaconState.ACTIVE || pool <= 0) return 0;
        double threshold = Math.min(flickerThreshold(), capacity());
        double light = pool >= threshold
                ? 6 + 9 * pool / capacity()
                : (6 + 9 * threshold / capacity()) * pool / threshold;
        return Mth.clamp((int) Math.ceil(light), 1, 15);
    }

    /** Whether a player can respawn here */
    public boolean isUsable() {
        return state == RespawnBeaconState.ACTIVE && pool > 0;
    }

    public GlobalPos pos() {
        return pos;
    }

    public double mana() {
        return pool;
    }

    public RespawnBeaconState state() {
        return state;
    }

    public Optional<UUID> placer() {
        return placer;
    }
}