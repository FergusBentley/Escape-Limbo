package com.fergusb.escapelimbo.fabric;

import com.fergusb.escapelimbo.platform.Platform;
import java.nio.file.Path;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

final class FabricPlatform implements Platform {
    @Override
    public Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public <R, T extends R> Supplier<T> register(Registry<R> registry, Identifier id, Supplier<T> factory) {
        T value = Registry.register(registry, id, factory.get());
        return () -> value;
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }
}