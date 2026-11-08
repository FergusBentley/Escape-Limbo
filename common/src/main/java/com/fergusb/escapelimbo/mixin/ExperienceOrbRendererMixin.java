package com.fergusb.escapelimbo.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.entity.ExperienceOrbRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Recolor xp orbs */
@Mixin(ExperienceOrbRenderer.class)
abstract class ExperienceOrbRendererMixin {
    @WrapOperation(method = "vertex", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;setColor(IIII)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private static VertexConsumer escapeLimbo$manaColor(VertexConsumer consumer, int red, int green, int blue, int alpha, Operation<VertexConsumer> original) {
        return original.call(consumer, 40 + blue, 170 + red / 3, 255, alpha);
    }
}