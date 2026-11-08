package com.fergusb.escapelimbo.fabric;

import com.fergusb.escapelimbo.EscapeLimbo;
import com.fergusb.escapelimbo.registry.ModCreativeTab;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public class EscapeLimboFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        EscapeLimbo.init(new FabricPlatform());

        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ModCreativeTab.KEY,
                FabricCreativeModeTab.builder().title(ModCreativeTab.TITLE).icon(ModCreativeTab::icon).build());
        CreativeModeTabEvents.modifyOutputEvent(ModCreativeTab.KEY).register(output -> {
            for (ItemStack stack : ModCreativeTab.contents()) output.accept(stack);
        });

    }
}