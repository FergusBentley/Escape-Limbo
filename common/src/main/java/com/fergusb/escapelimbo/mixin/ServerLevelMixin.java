package com.fergusb.escapelimbo.mixin;

import com.fergusb.escapelimbo.mana.PlantMana;
import com.fergusb.escapelimbo.survival.NightSkip;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.ClockTimeMarker;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
abstract class ServerLevelMixin {
    // Measures how much time sleeping skipped
    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/clock/ServerClockManager;moveToTimeMarker(Lnet/minecraft/core/Holder;Lnet/minecraft/resources/ResourceKey;)Lnet/minecraft/world/clock/ServerClockManager$MoveResult;"))
    private ServerClockManager.MoveResult escapeLimbo$nightSkipped(ServerClockManager clocks, Holder<WorldClock> clock, ResourceKey<ClockTimeMarker> marker,
                                                                   Operation<ServerClockManager.MoveResult> original) {
        ServerLevel level = (ServerLevel) (Object) this;
        long before = level.getDefaultClockTime();
        ServerClockManager.MoveResult result = original.call(clocks, clock, marker);
        NightSkip.onNightSkipped(level.getServer(), level.getDefaultClockTime() - before);
        return result;
    }

    // Finds Pitcher Plants
    @Inject(method = "updatePOIOnBlockStateChange", at = @At("HEAD"))
    private void escapeLimbo$plants(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
        PlantMana.onBlockChange((ServerLevel) (Object) this, pos, oldState, newState);
    }
}