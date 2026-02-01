package net.tcmfatbird.tutorialmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.tcmfatbird.tutorialmod.block.ModBlocks;
import net.tcmfatbird.tutorialmod.command.CustomCommands;
import net.tcmfatbird.tutorialmod.feature.BlockHighlightTracker;
import net.tcmfatbird.tutorialmod.feature.ChatMentions;
import net.tcmfatbird.tutorialmod.network.ClockTogglePacket;
import net.tcmfatbird.tutorialmod.item.ModItemGroups;
import net.tcmfatbird.tutorialmod.item.ModItems;
import net.tcmfatbird.tutorialmod.network.BlockHighlightPacket;
import net.tcmfatbird.tutorialmod.util.ChatFormatter;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TutorialMod implements ModInitializer {
    public static final String MOD_ID = "tutorialmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(ClockTogglePacket.ID, ClockTogglePacket.CODEC);
        PayloadTypeRegistry.playS2C().register(BlockHighlightPacket.ID, BlockHighlightPacket.CODEC);

        ModItemGroups.registerItemGroups();
        ModItems.registerModItems();
        ModBlocks.registerModBlocks();
        CustomCommands.register();

        FuelRegistry.INSTANCE.add(ModItems.STARLIGHT_ASHES, 600);

        // --- CHAT HOOK ---
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            String content = message.getContent().getString();

            // --- CHAT MENTIONS ---
            String mentionError = ChatMentions.processMentions(sender, content);
            if (mentionError != null) {
                // Cooldown hit — send error only to the sender, don't broadcast
                sender.sendMessage(Text.literal(mentionError)
                        .setStyle(net.minecraft.text.Style.EMPTY
                                .withColor(net.minecraft.text.TextColor.fromRgb(0xFF4444))), false);
                return false; // cancel the message entirely
            }

            // --- BLOCK HIGHLIGHT CHECK ---
            BlockPos highlighted = BlockHighlightTracker.onChat(sender, content);
            if (highlighted != null) {
                ServerPlayNetworking.send(sender, new BlockHighlightPacket(highlighted));
            }

            // --- CHAT FORMATTING ---
            MutableText formatted = ChatFormatter.parseForPlayer(content, sender);
            MutableText fullMessage = Text.literal("<" + sender.getName().getString() + "> ")
                    .append(formatted);

            sender.getServer().getPlayerManager().broadcast(fullMessage, false);

            return false; // cancel default
        });
    }
}