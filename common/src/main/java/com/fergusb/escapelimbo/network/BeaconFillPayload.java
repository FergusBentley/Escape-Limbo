package com.fergusb.escapelimbo.network;

import com.fergusb.escapelimbo.EscapeLimbo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record BeaconFillPayload(int mana, int capacity) implements CustomPacketPayload {
    public static final Type<BeaconFillPayload> TYPE = new Type<>(EscapeLimbo.id("beacon_fill"));
    public static final StreamCodec<ByteBuf, BeaconFillPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BeaconFillPayload::mana,
            ByteBufCodecs.VAR_INT, BeaconFillPayload::capacity,
            BeaconFillPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}