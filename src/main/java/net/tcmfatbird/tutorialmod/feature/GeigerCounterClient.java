package net.tcmfatbird.tutorialmod.feature;

import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;

public class GeigerCounterClient {

    private static int radiationLevel = 0;
    private static int tickCounter = 0;
    private static int nearestDistance = Integer.MAX_VALUE;

    public static void setNearestDistance(int distance) {
        nearestDistance = distance;
    }

    public static int getRadiationLevel() {
        return radiationLevel;
    }
    public static int getNearestDistance() {
        return nearestDistance;
    }

    // Beep every N ticks depending on level
    private static final int[] BEEP_INTERVALS = {40, 15, 4};

    public static void setRadiationLevel(int level) {
        radiationLevel = level;
    }

    public static void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) return;

        tickCounter++;
        int interval = BEEP_INTERVALS[radiationLevel];

        if (tickCounter >= interval) {
            tickCounter = 0;
            client.world.playSound(
                    client.player.getX(),
                    client.player.getY(),
                    client.player.getZ(),
                    SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON,
                    SoundCategory.PLAYERS,
                    radiationLevel == 2 ? 0.4f : 0.2f,
                    radiationLevel == 2 ? 1.8f : 1.2f,
                    false
            );
        }
    }

    public static void reset() {
        radiationLevel = 0;
        tickCounter = 0;
        nearestDistance = Integer.MAX_VALUE;
    }
}