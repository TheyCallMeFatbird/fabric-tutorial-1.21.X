package net.tcmfatbird.tutorialmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TutorialModClient implements ClientModInitializer {
    private int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null || client.player == null) return;

            tickCounter++;
            if (tickCounter < 20) return; // update once per second
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
