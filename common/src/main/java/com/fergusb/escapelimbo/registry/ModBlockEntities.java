package com.fergusb.escapelimbo.registry;

import com.fergusb.escapelimbo.EscapeLimbo;
import com.fergusb.escapelimbo.beacon.RespawnBeaconBlockEntity;
import com.fergusb.escapelimbo.platform.Platform;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static Supplier<BlockEntityType<RespawnBeaconBlockEntity>> RESPAWN_BEACON;

    private ModBlockEntities() {}

    public static void register(Platform platform) {
        RESPAWN_BEACON = platform.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, EscapeLimbo.id("respawn_beacon"),
                () -> new BlockEntityType<>(RespawnBeaconBlockEntity::new, Set.of(ModBlocks.RESPAWN_BEACON.get())));
    }
}