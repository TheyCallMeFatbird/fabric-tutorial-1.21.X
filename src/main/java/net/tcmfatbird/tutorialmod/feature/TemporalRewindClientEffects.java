package net.tcmfatbird.tutorialmod.feature;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public final class TemporalRewindClientEffects {
    private static boolean rewinding;
    private static int soundTick;
    private static int overlayTick;

    private TemporalRewindClientEffects() {}

    public static void register() {
        HudRenderCallback.EVENT.register(TemporalRewindClientEffects::render);
    }

    public static void setRewinding(boolean active) {
        rewinding = active;
        if (!active) {
            soundTick = 0;
            overlayTick = 0;
        }
    }

    public static void tick(MinecraftClient client) {
        if (!rewinding || client.player == null || client.world == null) {
            return;
        }

        overlayTick++;
        soundTick++;

        if (soundTick >= 6) {
            soundTick = 0;
            float pitch = (overlayTick / 6 % 2 == 0) ? 1.8f : 0.55f;
            client.world.playSound(
                    client.player.getX(),
                    client.player.getY(),
                    client.player.getZ(),
                    SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE,
                    SoundCategory.PLAYERS,
                    0.35f,
                    pitch,
                    false
            );
        }
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        if (!rewinding) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }

        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();
        int pulse = 55 + (overlayTick % 20);
        int overlayColor = (pulse << 24) | 0x2233AA;

        context.fill(0, 0, width, height, overlayColor);
        context.drawText(client.textRenderer, "⏪ REWINDING", width / 2 - 45, height / 2 - 35, 0x99BBFF, true);
    }
}