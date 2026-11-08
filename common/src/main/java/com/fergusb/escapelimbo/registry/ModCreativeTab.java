package com.fergusb.escapelimbo.registry;

import com.fergusb.escapelimbo.EscapeLimbo;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTab {
    public static final ResourceKey<CreativeModeTab> KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, EscapeLimbo.id("escape_limbo"));
    public static final Component TITLE = Component.translatable("itemGroup.escape_limbo");

    private ModCreativeTab() {}

    public static ItemStack icon() {
        return new ItemStack(ModItems.SOUL_FRAGMENT.get());
    }

    public static List<ItemStack> contents() {
        return List.of(
                new ItemStack(ModItems.SOUL_FRAGMENT.get()),
                new ItemStack(ModItems.REALITY_FABRIC.get()));
    }
}