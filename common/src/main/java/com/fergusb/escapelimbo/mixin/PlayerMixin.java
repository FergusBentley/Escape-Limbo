package com.fergusb.escapelimbo.mixin;

import com.fergusb.escapelimbo.survival.Hunger;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
abstract class PlayerMixin {
    @ModifyVariable(method = "causeFoodExhaustion", at = @At("HEAD"), argsOnly = true)
    private float escapeLimbo$scaleExhaustion(float amount) {
        return Hunger.scaleExhaustion((Player) (Object) this, amount);
    }
}