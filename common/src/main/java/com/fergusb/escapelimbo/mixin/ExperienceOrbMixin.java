package com.fergusb.escapelimbo.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Mending does not automatically work when receiving mana */
@Mixin(ExperienceOrb.class)
abstract class ExperienceOrbMixin {
    @Inject(method = "repairPlayerItems", at = @At("HEAD"), cancellable = true)
    private void escapeLimbo$noMending(ServerPlayer player, int amount, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(amount);
    }
}