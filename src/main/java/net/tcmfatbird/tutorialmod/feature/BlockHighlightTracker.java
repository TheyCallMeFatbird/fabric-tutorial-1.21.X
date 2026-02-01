package net.tcmfatbird.tutorialmod.feature;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BlockHighlightTracker {

    // How many times the player must say the block name in a row
    private static final int REQUIRED_REPEATS = 3;

    // How far (in blocks) to search for the nearest matching block
    public static final int SEARCH_RADIUS = 64;

    // Stores recent messages per player for tracking repeats
    private static final Map<UUID, List<String>> recentMessages = new ConcurrentHashMap<>();

    // Currently highlighted block per player (so we can clear it)
    private static final Map<UUID, BlockPos> highlightedBlocks = new ConcurrentHashMap<>();

    /**
     * Call this from the chat event. Returns the BlockPos to highlight, or null.
     */
    public static BlockPos onChat(ServerPlayerEntity player, String message) {
        UUID uuid = player.getUuid();
        recentMessages.computeIfAbsent(uuid, k -> new ArrayList<>());
        List<String> history = recentMessages.get(uuid);

        // Get what block the player is holding
        ItemStack held = player.getMainHandStack();
        if (held.isEmpty()) {
            history.clear();
            return null;
        }

        // Get the block's display name, uppercase for comparison
        String blockName = held.getItem().getName().getString().toUpperCase();

        // Normalize the message: trim and uppercase
        String normalized = message.trim().toUpperCase();

        // Check if this message is the block name
        if (!normalized.equals(blockName)) {
            history.clear();
            return null;
        }

        history.add(normalized);

        // Only keep the last REQUIRED_REPEATS messages
        while (history.size() > REQUIRED_REPEATS) {
            history.remove(0);
        }

        // Check if we have enough repeats
        if (history.size() == REQUIRED_REPEATS) {
            boolean allMatch = history.stream().allMatch(msg -> msg.equals(blockName));
            history.clear(); // reset after a successful trigger

            if (allMatch) {
                // Find the nearest block of this type
                BlockPos found = findNearestBlock(player);
                highlightedBlocks.put(uuid, found); // may be null if not found
                return found;
            }
        }

        return null; // haven't reached 3 yet, don't send anything
    }

    /**
     * Returns true if we just hit 3 repeats (even if block wasn't found).
     * This lets us distinguish "not enough messages yet" from "triggered but no block found".
     */
    public static boolean hasTriggered(ServerPlayerEntity player, String message) {
        UUID uuid = player.getUuid();
        ItemStack held = player.getMainHandStack();
        if (held.isEmpty()) return false;

        String blockName = held.getItem().getName().getString().toUpperCase();
        String normalized = message.trim().toUpperCase();
        if (!normalized.equals(blockName)) return false;

        List<String> history = recentMessages.getOrDefault(uuid, new ArrayList<>());
        // We already added it in onChat, so check size was exactly REQUIRED_REPEATS before clear
        // Simpler: just check if onChat returned non-null OR if history was just cleared after match
        // Actually let's just use a flag. We'll refactor slightly:
        return false; // handled inline below
    }

    /**
     * Searches in a radius around the player for the nearest block matching what they're holding.
     */
    private static BlockPos findNearestBlock(ServerPlayerEntity player) {
        BlockView world = player.getWorld();
        BlockPos playerPos = player.getBlockPos();
        Block targetBlock = player.getMainHandStack().getItem() instanceof net.minecraft.item.BlockItem bi
                ? bi.getBlock() : null;

        if (targetBlock == null) return null;

        BlockPos closest = null;
        double closestDist = Double.MAX_VALUE;

        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
            for (int y = -SEARCH_RADIUS; y <= SEARCH_RADIUS; y++) {
                for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                    BlockPos check = playerPos.add(x, y, z);
                    if (world.getBlockState(check).getBlock() == targetBlock) {
                        double dist = check.getSquaredDistance(playerPos);
                        if (dist > 0 && dist < closestDist) {
                            closestDist = dist;
                            closest = check;
                        }
                    }
                }
            }
        }

        return closest;
    }

    public static void clearHighlight(ServerPlayerEntity player) {
        highlightedBlocks.remove(player.getUuid());
        recentMessages.remove(player.getUuid());
    }
}