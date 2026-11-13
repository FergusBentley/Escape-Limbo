package com.fergusb.escapelimbo.platform;

import java.nio.file.Path;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/** Loader-specific services */
public interface Platform {
    Path configDir();

    <R, T extends R> Supplier<T> register(Registry<R> registry, Identifier id, Supplier<T> factory);

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);
}