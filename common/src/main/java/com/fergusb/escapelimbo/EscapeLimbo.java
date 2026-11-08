package com.fergusb.escapelimbo;

import com.fergusb.escapelimbo.config.EscapeLimboConfig;
import com.fergusb.escapelimbo.platform.Platform;
import com.fergusb.escapelimbo.registry.ModBlocks;
import com.fergusb.escapelimbo.registry.ModItems;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EscapeLimbo {
    public static final String MOD_ID = "escape_limbo";
    public static final Logger LOGGER = LoggerFactory.getLogger("Escape Limbo");
    private static Platform platform;
    private static EscapeLimboConfig config;

    private EscapeLimbo() {}

    public static void init(Platform loaderPlatform) {
        platform = loaderPlatform;
        config = EscapeLimboConfig.load(platform.configDir());
        ModBlocks.register(platform);
        ModItems.register(platform);
    }

    public static Platform platform() {
        return platform;
    }

    public static EscapeLimboConfig config() {
        return config;
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}