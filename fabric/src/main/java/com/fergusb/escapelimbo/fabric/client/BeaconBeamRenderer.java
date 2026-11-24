package com.fergusb.escapelimbo.fabric.client;

import com.fergusb.escapelimbo.beacon.BeaconDisplay;
import com.fergusb.escapelimbo.beacon.RespawnBeaconBlock;
import com.fergusb.escapelimbo.network.BeaconBeamsPayload;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Draws Respawn Beacon beams */
final class BeaconBeamRenderer {
    private static final int RGB = 0xFFFFFF;

    private static final float FAINTEST = 0.35F;

    private static @Nullable BeaconBeamsPayload latest;

    private BeaconBeamRenderer() {}

    static void update(BeaconBeamsPayload payload) {
        latest = payload;
    }

    static void render(LevelRenderContext context) {
        ClientLevel level = Minecraft.getInstance().level;
        BeaconBeamsPayload payload = latest;
        if (level == null || payload == null || payload.dimension() != level.dimension() || payload.beams().isEmpty()) return;

        Vec3 camera = context.levelState().cameraRenderState.pos;
        PoseStack poseStack = context.poseStack();
        float maxDistance = Minecraft.getInstance().options.getEffectiveRenderDistance() * 16.0F;

        for (BeaconBeamsPayload.Beam beam : payload.beams()) {
            BlockPos pos = beam.pos();
            if (level.isLoaded(pos) && !(level.getBlockState(pos).getBlock() instanceof RespawnBeaconBlock)) continue;

            float dx = (float) (pos.getX() + 0.5 - camera.x);
            float dz = (float) (pos.getZ() + 0.5 - camera.z);
            float distance = Mth.sqrt(dx * dx + dz * dz);
            float alpha = Mth.clamp(0.9F - distance / 1000.0F, 0.25F, 0.9F) * (1 - (1 - FAINTEST) * beam.dim());
            // A far beam is drawn closer and scaled down by the same factor
            float scale = distance > maxDistance ? maxDistance / distance : 1.0F;
            float x = dx * scale;
            float z = dz * scale;
            float bottom = (float) (pos.getY() + BeaconDisplay.PEDESTAL_TOP - camera.y);
            float blockTop = (float) (pos.getY() + 1 - camera.y) * scale;
            float top = (float) (level.getMaxY() - camera.y) * scale;
            float radius = Math.max(0.15F, distance * 0.004F) * scale;

            int core = argb(alpha, RGB);
            int glow = argb(alpha * 0.3F * (1 - beam.dim()), RGB);
            context.submitNodeCollector().submitCustomGeometry(poseStack, RenderTypes.debugQuads(), (pose, consumer) -> {
                column(pose, consumer, x, z, bottom, blockTop, radius, core);
                column(pose, consumer, x, z, bottom, blockTop, radius * 2.5F, glow);
            });
            submitAfterTerrain(context.submitNodeCollector(), poseStack, (pose, consumer) -> {
                column(pose, consumer, x, z, blockTop, top, radius, core);
                column(pose, consumer, x, z, blockTop, top, radius * 2.5F, glow);
            });
        }
    }

    private static void submitAfterTerrain(SubmitNodeCollector collector, PoseStack poseStack, SubmitNodeCollector.CustomGeometryRenderer geometry) {
        if (collector instanceof SubmitNodeStorage storage) {
            storage.order(0).afterTerrain.submit(new CustomFeatureRenderer.Submit(poseStack.last().copy(), RenderTypes.debugQuads(), geometry));
        } else {
            collector.submitCustomGeometry(poseStack, RenderTypes.debugQuads(), geometry);
        }
    }

    private static int argb(float alpha, int rgb) {
        return ((int) (alpha * 255) << 24) | rgb;
    }

    private static void column(PoseStack.Pose pose, VertexConsumer consumer, float x, float z, float y0, float y1, float r, int color) {
        quad(pose, consumer, x - r, z - r, x + r, z - r, y0, y1, color);
        quad(pose, consumer, x + r, z - r, x + r, z + r, y0, y1, color);
        quad(pose, consumer, x + r, z + r, x - r, z + r, y0, y1, color);
        quad(pose, consumer, x - r, z + r, x - r, z - r, y0, y1, color);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer consumer, float xa, float za, float xb, float zb, float y0, float y1, int color) {
        consumer.addVertex(pose, xa, y0, za).setColor(color);
        consumer.addVertex(pose, xb, y0, zb).setColor(color);
        consumer.addVertex(pose, xb, y1, zb).setColor(color);
        consumer.addVertex(pose, xa, y1, za).setColor(color);
    }
}