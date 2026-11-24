package com.fergusb.escapelimbo.network;

import com.fergusb.escapelimbo.EscapeLimbo;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/** Every active Respawn Beacon beam in the player's dimension */
public record BeaconBeamsPayload(ResourceKey<Level> dimension, List<Beam> beams) implements CustomPacketPayload {
    public static final Type<BeaconBeamsPayload> TYPE = new Type<>(EscapeLimbo.id("beacon_beams"));
    public static final StreamCodec<ByteBuf, BeaconBeamsPayload> CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION), BeaconBeamsPayload::dimension,
            Beam.CODEC.apply(ByteBufCodecs.list()), BeaconBeamsPayload::beams,
            BeaconBeamsPayload::new);

    /** dim: 0 = full brightness, 1 = faint */
    public record Beam(BlockPos pos, float dim) {
        public static final StreamCodec<ByteBuf, Beam> CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, Beam::pos,
                ByteBufCodecs.FLOAT, Beam::dim,
                Beam::new);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}