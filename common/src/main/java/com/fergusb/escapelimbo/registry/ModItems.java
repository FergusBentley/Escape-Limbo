package com.fergusb.escapelimbo.registry;

import com.fergusb.escapelimbo.EscapeLimbo;
import com.fergusb.escapelimbo.platform.Platform;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class ModItems {
    public static Supplier<Item> REALITY_FABRIC;
    public static Supplier<Item> SOUL_FRAGMENT;

    private ModItems() {}

    public static void register(Platform platform) {
        REALITY_FABRIC = blockItem(platform, "reality_fabric", ModBlocks.REALITY_FABRIC);
        SOUL_FRAGMENT = item(platform, "soul_fragment", Item::new, new Item.Properties());
    }

    private static Supplier<Item> item(Platform platform, String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
        Identifier id = EscapeLimbo.id(name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        return platform.register(BuiltInRegistries.ITEM, id, () -> factory.apply(properties.setId(key)));
    }

    private static Supplier<Item> blockItem(Platform platform, String name, Supplier<Block> block) {
        Identifier id = EscapeLimbo.id(name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        return platform.register(BuiltInRegistries.ITEM, id,
                () -> new BlockItem(block.get(), new Item.Properties().setId(key).useBlockDescriptionPrefix()));
    }
}