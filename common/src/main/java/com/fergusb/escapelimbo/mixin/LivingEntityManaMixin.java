package com.fergusb.escapelimbo.mixin;

import com.fergusb.escapelimbo.mana.MobMana;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Mob mana drops */
@Mixin(LivingEntity.class)
abstract class LivingEntityManaMixin {
    @WrapOperation(method = "dropExperience", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getExperienceReward(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)I"))
    private int escapeLimbo$mana(LivingEntity mob, ServerLevel level, Entity killer, Operation<Integer> original) {
        return MobMana.onDrop(mob, level, original.call(mob, level, killer));
    }
}