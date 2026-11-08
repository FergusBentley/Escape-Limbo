package com.fergusb.escapelimbo.mana;

import com.fergusb.escapelimbo.EscapeLimbo;
import net.minecraft.world.entity.player.Player;

/** Reskin XP as mana */
public final class Mana {
    private static final String STARTED_TAG = "escape_limbo.starting_mana";

    private Mana() {}

    /** Starting mana */
    public static void onJoin(Player player) {
        if (player.entityTags().contains(STARTED_TAG)) return;
        player.addTag(STARTED_TAG);
        int mana = EscapeLimbo.config().startingMana;
        if (mana > 0) player.giveExperiencePoints(mana);
    }

    /** Current mana */
    public static int points(Player player) {
        int total = 0;
        for (int level = 0; level < player.experienceLevel; level++) total += pointsForLevel(level);
        return total + Math.round(player.experienceProgress * pointsForLevel(player.experienceLevel));
    }

    /** Remove mana from player */
    public static int take(Player player, int amount) {
        if (player.isCreative()) return amount;
        int taken = Math.min(amount, points(player));
        if (taken > 0) player.giveExperiencePoints(-taken);
        return taken;
    }

    /** From Player.getXpNeededForNextLevel */
    private static int pointsForLevel(int level) {
        if (level >= 30) return 112 + (level - 30) * 9;
        if (level >= 15) return 37 + (level - 15) * 5;
        return 7 + level * 2;
    }
}