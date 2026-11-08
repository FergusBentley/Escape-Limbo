package com.fergusb.escapelimbo.mixin;

import com.fergusb.escapelimbo.mana.ManaHud;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Recolor xp level */
@Mixin(Hud.class)
abstract class HudMixin {
    @WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V"))
    private void escapeLimbo$manaLevel(GuiGraphicsExtractor graphics, Font font, int level, Operation<Void> original) {
        ManaHud.extractLevel(graphics, font, level);
    }
}