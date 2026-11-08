package com.fergusb.escapelimbo.survival;

import com.fergusb.escapelimbo.EscapeLimbo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;

/** Rescale exhaustion so that you always have to eat */
public final class Hunger {
    private Hunger() {}

    public static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.isCreative() || player.isSpectator() || !player.isAlive()) continue;
            if (player.level().getDifficulty() == Difficulty.PEACEFUL) continue;
            player.getFoodData().addExhaustion((float) EscapeLimbo.config().passiveHungerPerTick);
        }
    }

    public static float scaleExhaustion(Player player, float amount) {
        return (float) (amount * EscapeLimbo.config().otherHungerMultiplier);
    }
}