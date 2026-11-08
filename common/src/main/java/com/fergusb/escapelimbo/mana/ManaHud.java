package com.fergusb.escapelimbo.mana;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class ManaHud {
    public static final int LEVEL_COLOR = 0xFF7FE8FF;
    private static final int OUTLINE = 0xFF000000;

    private ManaHud() {}

    public static void extractLevel(GuiGraphicsExtractor graphics, Font font, int level) {
        Component text = Component.translatable("gui.experience.level", level);
        int x = (graphics.guiWidth() - font.width(text)) / 2;
        int y = graphics.guiHeight() - 24 - font.lineHeight - 2;
        graphics.text(font, text, x + 1, y, OUTLINE, false);
        graphics.text(font, text, x - 1, y, OUTLINE, false);
        graphics.text(font, text, x, y + 1, OUTLINE, false);
        graphics.text(font, text, x, y - 1, OUTLINE, false);
        graphics.text(font, text, x, y, LEVEL_COLOR, false);
    }
}