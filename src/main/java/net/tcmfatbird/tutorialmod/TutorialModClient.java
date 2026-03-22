package net.tcmfatbird.tutorialmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.tcmfatbird.tutorialmod.feature.BlockHighlightRenderer;
import net.tcmfatbird.tutorialmod.feature.GeigerCounterClient;
import net.tcmfatbird.tutorialmod.feature.GeigerHud;
import net.tcmfatbird.tutorialmod.gui.ClockScreen;
import net.tcmfatbird.tutorialmod.item.ModItems;
import net.tcmfatbird.tutorialmod.network.BlockHighlightPacket;
import net.tcmfatbird.tutorialmod.network.ClockTogglePacket;
import net.tcmfatbird.tutorialmod.network.NearestUraniumPacket;
import net.tcmfatbird.tutorialmod.network.RadiationLevelPacket;
import net.tcmfatbird.tutorialmod.network.TemporalRewindStatePacket;
import net.tcmfatbird.tutorialmod.network.TemporalRewindTogglePacket;
import org.lwjgl.glfw.GLFW;

public class TutorialModClient implements ClientModInitializer {
    private int tickCounter = 0;
    private boolean clockEnabled = true;

    private static KeyBinding clockGuiKey;
    private static KeyBinding temporalRewindKey;
    private boolean wasRewindPressed = false;

    @Override
    public void onInitializeClient() {
        GeigerHud.register();

        ClientPlayNetworking.registerGlobalReceiver(NearestUraniumPacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                GeigerCounterClient.setNearestDistance(payload.distance());
            });
        });

        // --- KEYBIND (no InputUtil needed, just pass the int key code directly) ---
        clockGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.tutorialmod.clockgui",
                GLFW.GLFW_KEY_G,
                "key.category.tutorialmod"
        ));

        temporalRewindKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.tutorialmod.temporal_rewind",
                GLFW.GLFW_KEY_R,
                "key.category.tutorialmod"
        ));

        ClientPlayNetworking.registerGlobalReceiver(RadiationLevelPacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                GeigerCounterClient.setRadiationLevel(payload.level());
            });
        });

        // --- REGISTER C2S PACKET ---
        //PayloadTypeRegistry.playC2S().register(SetTimePacket.ID, SetTimePacket.CODEC);

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


        // --- TICK ---
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.player == null) {
                GeigerCounterClient.reset();
            } else {
                boolean holdingGeiger = client.player.getMainHandStack().isOf(ModItems.GEIGER_COUNTER)
                        || client.player.getOffHandStack().isOf(ModItems.GEIGER_COUNTER);

                if (holdingGeiger) {
                    GeigerCounterClient.tick(client);
                } else {
                    GeigerCounterClient.reset();
                }
            }

            BlockHighlightRenderer.tick();

            // Open clock GUI on keypress
            while (clockGuiKey.wasPressed()) {
                if (client.world != null) {
                    client.setScreen(new ClockScreen());
                }
            }

            boolean rewindPressed = temporalRewindKey.isPressed();
            if (client.player != null && rewindPressed != wasRewindPressed) {
                ClientPlayNetworking.send(new TemporalRewindTogglePacket(rewindPressed));
                wasRewindPressed = rewindPressed;
            } else if (client.player == null) {
                wasRewindPressed = false;
            }

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
                    true
            );
        });
    }
}