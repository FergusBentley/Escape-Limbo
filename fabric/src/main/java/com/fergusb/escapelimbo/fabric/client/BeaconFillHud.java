package com.fergusb.escapelimbo.fabric.client;

import com.fergusb.escapelimbo.network.BeaconFillPayload;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

/** Small bar above the hotbar while a player is putting mana into a beacon */
final class BeaconFillHud {
    private static final long SHOW_MS = 1500;
    private static final long FADE_MS = 400;
    private static final int WIDTH = 120;
    private static final int HEIGHT = 5;

    private static int mana;
    private static int capacity = 1;
    private static long lastUpdate = -SHOW_MS;

    private BeaconFillHud() {}

    static void update(BeaconFillPayload payload) {
        mana = payload.mana();
        capacity = Math.max(1, payload.capacity());
        lastUpdate = Util.getMillis();
    }

    static void extract(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        long age = Util.getMillis() - lastUpdate;
        if (age > SHOW_MS) return;
        float alpha = age < SHOW_MS - FADE_MS ? 1.0F : (SHOW_MS - age) / (float) FADE_MS;
        int a = Mth.clamp((int) (alpha * 255), 8, 255) << 24;

        Minecraft minecraft = Minecraft.getInstance();
        int x = (graphics.guiWidth() - WIDTH) / 2;
        int y = graphics.guiHeight() - 68;
        int filled = Math.round(WIDTH * Math.min(1.0F, mana / (float) capacity));

        graphics.fill(x - 1, y - 1, x + WIDTH + 1, y + HEIGHT + 1, a | 0x101820);
        graphics.fill(x, y, x + filled, y + HEIGHT, a | 0x6FE3FF);
        graphics.centeredText(minecraft.font, mana + " / " + capacity, graphics.guiWidth() / 2, y - 11, a | 0xBFF4FF);
    }
}