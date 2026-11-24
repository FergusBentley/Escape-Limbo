package com.fergusb.escapelimbo.mixin;

import com.fergusb.escapelimbo.EscapeLimbo;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.commands.RenderPass;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draw Respawn Beacon beams last */
@Mixin(LevelRenderer.class)
abstract class LevelRendererMixin {
    @Unique
    private boolean escapeLimbo$deferred;

    @WrapOperation(method = "executeClassicTransparency", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;executeTranslucentAfterTerrain(Lcom/mojang/renderpearl/api/commands/RenderPass;)V"))
    private void escapeLimbo$deferAfterTerrain(FeatureRenderDispatcher.PreparedFrame frame, RenderPass pass, Operation<Void> original) {
        if (EscapeLimbo.config().beamsAboveWeather) escapeLimbo$deferred = true;
        else original.call(frame, pass);
    }

    @Inject(method = "executeClassicTransparency", at = @At("TAIL"), require = 0)
    private void escapeLimbo$afterTerrainLast(ChunkSectionsToRender sections, FeatureRenderDispatcher.PreparedFrame frame, RenderPass pass, CallbackInfo ci) {
        if (!escapeLimbo$deferred) return;
        escapeLimbo$deferred = false;
        frame.executeTranslucentAfterTerrain(pass);
    }
}