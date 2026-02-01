package net.tcmfatbird.tutorialmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.tcmfatbird.tutorialmod.feature.BlockHighlightRenderer;
import net.tcmfatbird.tutorialmod.network.BlockHighlightPacket;
import net.tcmfatbird.tutorialmod.network.ClockTogglePacket;

public class TutorialModClient implements ClientModInitializer {
    private int tickCounter = 0;
    private boolean clockEnabled = true;

    @Override
    public void onInitializeClient() {
        // --- BLOCK HIGHLIGHT ---
        BlockHighlightRenderer.register();

        ClientPlayNetworking.registerGlobalReceiver(BlockHighlightPacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.pos() != null) {
                    BlockHighlightRenderer.setHighlight(payload.pos());
                } else {
                    BlockHighlightRenderer.highlightedBlock = null;
                }
            });
        });

        // --- CLOCK TOGGLE PACKET ---
        ClientPlayNetworking.registerGlobalReceiver(ClockTogglePacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                clockEnabled = payload.enabled();
            });
        });

        // --- TICK (time display + highlight timer) ---
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Highlight timer
            BlockHighlightRenderer.tick();

            // Clock display
            if (!clockEnabled) return;
            if (client.world == null || client.player == null) return;

            tickCounter++;
            if (tickCounter < 20) return;
            tickCounter = 0;

            long time = client.world.getTimeOfDay() % 24000;

            int hours = (int) ((time + 6000) % 24000 / 1000);
            int minutes = (int) (((time + 6000) % 1000) * 60 / 1000);

            String timeString = String.format("%02d:%02d", hours, minutes);

            client.player.sendMessage(
                    Text.literal("⏰ Time: " + timeString)
                            .formatted(Formatting.YELLOW, Formatting.BOLD),
                    true // actionbar
            );
        });
    }
}