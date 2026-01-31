package net.tcmfatbird.tutorialmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.tcmfatbird.tutorialmod.block.ModBlocks;
import net.tcmfatbird.tutorialmod.command.CustomCommands;
import net.tcmfatbird.tutorialmod.item.ModItemGroups;
import net.tcmfatbird.tutorialmod.item.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.tcmfatbird.tutorialmod.util.ChatFormatter;

public class TutorialMod implements ModInitializer {
    public static final String MOD_ID = "tutorialmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItemGroups.registerItemGroups();
        ModItems.registerModItems();
        ModBlocks.registerModBlocks();
        CustomCommands.register();

        FuelRegistry.INSTANCE.add(ModItems.STARLIGHT_ASHES, 600);

        // --- CHAT FORMAT HOOK ---
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            // Get the plain text content
            String content = message.getContent().getString();

            // Format the message
            MutableText formatted = ChatFormatter.parseForPlayer(content, sender);

            // Create the full chat message with username
            MutableText fullMessage = Text.literal("<" + sender.getName().getString() + "> ")
                    .append(formatted);

            // Broadcast to all players
            sender.getServer().getPlayerManager().broadcast(
                    fullMessage,
                    false // not an overlay message
            );

            // Cancel the default chat message
            return false;
        });
    }
}