package net.tcmfatbird.tutorialmod.feature;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class BlockHighlightRenderer {

    public static volatile BlockPos highlightedBlock = null;

    private static final int HIGHLIGHT_DURATION_TICKS = 60;
    private static int remainingTicks = 0;

    private static final float R = 0.0f;
    private static final float G = 1.0f;
    private static final float B = 0.5f;
    private static final float A = 0.8f;
    private static final float LINE_WIDTH = 5.0f;

    public static void register() {
        // BEFORE_ENTITIES is better here — it runs every frame and doesn't
        // require a block hit to fire, unlike BEFORE_BLOCK_OUTLINE
        WorldRenderEvents.AFTER_ENTITIES.register(BlockHighlightRenderer::render);
    }

    public static void tick() {
        if (highlightedBlock != null) {
            remainingTicks--;
            if (remainingTicks <= 0) {
                highlightedBlock = null;
            }
        }
    }

    public static void setHighlight(BlockPos pos) {
        highlightedBlock = pos;
        remainingTicks = HIGHLIGHT_DURATION_TICKS;
    }

    // BEFORE_ENTITIES signature is just (WorldRenderContext) -> void
    private static void render(WorldRenderContext context) {
        if (highlightedBlock == null) return;

        Vec3d cameraPos = context.camera().getPos();
        Matrix4f matrix = context.matrixStack().peek().getPositionMatrix();

        float minX = highlightedBlock.getX() - (float) cameraPos.getX();
        float minY = highlightedBlock.getY() - (float) cameraPos.getY();
        float minZ = highlightedBlock.getZ() - (float) cameraPos.getZ();
        float maxX = minX + 1.0f;
        float maxY = minY + 1.0f;
        float maxZ = minZ + 1.0f;

        float pad = 0.002f;
        minX -= pad; minY -= pad; minZ -= pad;
        maxX += pad; maxY += pad; maxZ += pad;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.lineWidth(LINE_WIDTH);

        // Bottom face
        line(buffer, matrix, minX, minY, minZ, maxX, minY, minZ);
        line(buffer, matrix, maxX, minY, minZ, maxX, minY, maxZ);
        line(buffer, matrix, maxX, minY, maxZ, minX, minY, maxZ);
        line(buffer, matrix, minX, minY, maxZ, minX, minY, minZ);
        // Top face
        line(buffer, matrix, minX, maxY, minZ, maxX, maxY, minZ);
        line(buffer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ);
        line(buffer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ);
        line(buffer, matrix, minX, maxY, maxZ, minX, maxY, minZ);
        // Vertical edges
        line(buffer, matrix, minX, minY, minZ, minX, maxY, minZ);
        line(buffer, matrix, maxX, minY, minZ, maxX, maxY, minZ);
        line(buffer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ);
        line(buffer, matrix, minX, minY, maxZ, minX, maxY, maxZ);

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void line(BufferBuilder buffer, Matrix4f matrix,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2) {
        buffer.vertex(matrix, x1, y1, z1).color(R, G, B, A);
        buffer.vertex(matrix, x2, y2, z2).color(R, G, B, A);
    }
}