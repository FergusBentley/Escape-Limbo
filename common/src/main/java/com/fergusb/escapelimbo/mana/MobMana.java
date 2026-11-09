package com.fergusb.escapelimbo.mana;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

/** Adjust mana dropped by mobs */
public final class MobMana {
    private static final double BONUS = 1.2;

    private MobMana() {}

    public static int onDrop(LivingEntity mob, ServerLevel level, int amount) {
        if (mob instanceof Player) return amount;
        KillPenalty penalty = KillPenalty.get(level.getServer());
        String dimension = level.dimension().identifier().toString();
        ChunkPos chunk = ChunkPos.containing(mob.blockPosition());
        double scaled = amount * BONUS * (1 - penalty.penalty(level.getServer(), dimension, chunk));
        if (mob instanceof Enemy && mob.getLastHurtByPlayer() != null) penalty.recordKill(level.getServer(), dimension, chunk);
        return round(scaled, mob.getRandom());
    }

    private static int round(double value, RandomSource random) {
        int whole = (int) Math.floor(value);
        return whole + (random.nextDouble() < value - whole ? 1 : 0);
    }
}