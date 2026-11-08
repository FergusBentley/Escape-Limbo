package com.fergusb.escapelimbo.fabric;

import com.fergusb.escapelimbo.EscapeLimbo;
import com.fergusb.escapelimbo.command.EscapeLimboCommands;
import com.fergusb.escapelimbo.mana.Mana;
import com.fergusb.escapelimbo.registry.ModCreativeTab;
import com.fergusb.escapelimbo.survival.Hunger;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
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
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> EscapeLimboCommands.register(dispatcher));
        ServerTickEvents.END_SERVER_TICK.register(Hunger::tick);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> Mana.onJoin(handler.player));
    }
}