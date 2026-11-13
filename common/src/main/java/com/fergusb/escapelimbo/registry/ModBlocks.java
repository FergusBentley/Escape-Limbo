package com.fergusb.escapelimbo.registry;

import com.fergusb.escapelimbo.EscapeLimbo;
import com.fergusb.escapelimbo.beacon.BeaconDisplay;
import com.fergusb.escapelimbo.beacon.RespawnBeaconBlock;
import com.fergusb.escapelimbo.platform.Platform;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {
    public static Supplier<Block> REALITY_FABRIC;
    public static Supplier<Block> RESPAWN_BEACON;

    private ModBlocks() {}

    public static void register(Platform platform) {
        REALITY_FABRIC = block(platform, "reality_fabric", Block::new, limboProperties());
        RESPAWN_BEACON = block(platform, "respawn_beacon", RespawnBeaconBlock::new, BlockBehaviour.Properties.of()
                .mapColor(MapColor.DIAMOND)
                .strength(3.0F)
                .noLootTable()
                .noOcclusion()
                .sound(SoundType.GLASS)
                .lightLevel(BeaconDisplay::lightLevel));
    }

    private static BlockBehaviour.Properties limboProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .strength(-1.0F, 3600000.0F)
                .noLootTable()
                .sound(SoundType.STONE);
    }

    private static Supplier<Block> block(Platform platform, String name, Function<BlockBehaviour.Properties, Block> factory,
                                         BlockBehaviour.Properties properties) {
        Identifier id = EscapeLimbo.id(name);
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
        return platform.register(BuiltInRegistries.BLOCK, id, () -> factory.apply(properties.setId(key)));
    }
}