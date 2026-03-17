package net.tcmfatbird.tutorialmod.feature;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.tcmfatbird.tutorialmod.item.ModItems;
import org.joml.Matrix4f;

public final class QuantumVisionClient {
    private static final int RADIUS = 16;
    private static final int XZ_STEP = 4;
    private static final int Y_STEP = 3;

    private QuantumVisionClient() {}

    public static void register() {
        HudRenderCallback.EVENT.register(QuantumVisionClient::renderOverlay);
        WorldRenderEvents.AFTER_ENTITIES.register(QuantumVisionClient::renderOreClouds);
    }

    private static boolean active(MinecraftClient client) {
        return client.player != null
                && client.world != null
                && client.player.getEquippedStack(EquipmentSlot.HEAD).isOf(ModItems.WAVEFUNCTION_GOGGLES);
    }

    private static void renderOverlay(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!active(client)) return;

        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        context.fill(0, 0, width, height, 0x3311FF44);
        context.drawText(client.textRenderer, "~ WAVEFUNCTION VISION ~", width / 2 - 58, 8, 0xCCFFCC, true);
    }

    private static void renderOreClouds(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!active(client)) return;

        Vec3d cam = context.camera().getPos();
        Matrix4f matrix = context.matrixStack().peek().getPositionMatrix();

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        BlockPos center = client.player.getBlockPos();
        boolean wroteGeometry = false;

        for (int x = center.getX() - RADIUS; x <= center.getX() + RADIUS; x += XZ_STEP) {
            for (int z = center.getZ() - RADIUS; z <= center.getZ() + RADIUS; z += XZ_STEP) {
                for (int y = center.getY() - 10; y <= center.getY() + 6; y += Y_STEP) {
                    BlockPos anchor = new BlockPos(x, y, z);
                    BlockState state = client.world.getBlockState(anchor);
                    float chance = QuantumWavefunctionHandler.getCollapseChance(state, y);
                    if (chance <= 0f) continue;

                    float patchRoll = QuantumWavefunctionHandler.positionRoll(anchor);
                    if (patchRoll > chance * 2.2f) continue;

                    wroteGeometry = drawFluffyPatch(buffer, matrix, cam, anchor) || wroteGeometry;
                }
            }
        }

        if (wroteGeometry) {
            BufferRenderer.drawWithGlobalProgram(buffer.end());
        }

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static boolean drawFluffyPatch(BufferBuilder buffer, Matrix4f matrix, Vec3d cam, BlockPos anchor) {
        boolean wrote = false;
        int blobs = 7 + Math.floorMod(anchor.getX() + anchor.getY() + anchor.getZ(), 5);

        for (int i = 0; i < blobs; i++) {
            int sx = spread(anchor, i * 13 + 3);
            int sy = spread(anchor, i * 17 + 7) / 2;
            int sz = spread(anchor, i * 19 + 11);

            float ox = sx * 0.22f;
            float oy = sy * 0.18f;
            float oz = sz * 0.22f;

            float cx = anchor.getX() + 0.5f + ox;
            float cy = anchor.getY() + 0.45f + oy;
            float cz = anchor.getZ() + 0.5f + oz;

            float size = 0.22f + (Math.abs(spread(anchor, i * 23 + 5)) * 0.015f);
            float alpha = 0.10f + (0.03f * i);

            addCube(buffer, matrix,
                    cx - (float) cam.x,
                    cy - (float) cam.y,
                    cz - (float) cam.z,
                    size,
                    1.0f, 1.0f, 1.0f, Math.min(alpha, 0.28f));
            wrote = true;
        }

        return wrote;
    }

    private static int spread(BlockPos pos, int salt) {
        int hash = pos.getX() * 73428767 ^ pos.getY() * 9127837 ^ pos.getZ() * 43828921 ^ salt;
        hash ^= (hash >>> 13);
        return Math.floorMod(hash, 9) - 4;
    }

    private static void addCube(BufferBuilder buffer, Matrix4f matrix,
                                float cx, float cy, float cz,
                                float half,
                                float r, float g, float b, float a) {
        float minX = cx - half;
        float maxX = cx + half;
        float minY = cy - half;
        float maxY = cy + half;
        float minZ = cz - half;
        float maxZ = cz + half;

        quad(buffer, matrix, minX, minY, minZ, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, r, g, b, a);
        quad(buffer, matrix, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, minY, maxZ, r, g, b, a);

        quad(buffer, matrix, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, minX, minY, maxZ, r, g, b, a);
        quad(buffer, matrix, maxX, minY, minZ, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, r, g, b, a);

        quad(buffer, matrix, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
        quad(buffer, matrix, minX, minY, minZ, minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, r, g, b, a);
    }

    private static void quad(BufferBuilder buffer, Matrix4f matrix,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             float r, float g, float b, float a) {
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a);
        buffer.vertex(matrix, x3, y3, z3).color(r, g, b, a);
        buffer.vertex(matrix, x4, y4, z4).color(r, g, b, a);
    }
}
