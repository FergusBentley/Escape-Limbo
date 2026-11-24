package com.fergusb.escapelimbo.fabric.client;

import com.fergusb.escapelimbo.EscapeLimbo;
import com.fergusb.escapelimbo.network.BeaconBeamsPayload;
import com.fergusb.escapelimbo.network.BeaconFillPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

public class EscapeLimboFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(BeaconFillPayload.TYPE, (payload, context) -> BeaconFillHud.update(payload));
        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, EscapeLimbo.id("beacon_fill"), BeaconFillHud::extract);
        ClientPlayNetworking.registerGlobalReceiver(BeaconBeamsPayload.TYPE, (payload, context) -> BeaconBeamRenderer.update(payload));
        LevelRenderEvents.COLLECT_SUBMITS.register(BeaconBeamRenderer::render);
    }
}