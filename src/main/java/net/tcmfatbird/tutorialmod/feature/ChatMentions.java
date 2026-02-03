package net.tcmfatbird.tutorialmod.feature;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.tcmfatbird.tutorialmod.command.CustomCommands;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatMentions {

    private static final long MENTION_COOLDOWN_MS = 30_000;
    private static final Map<UUID, Long> lastMentionTime = new ConcurrentHashMap<>();
    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

    public static String processMentions(ServerPlayerEntity sender, String message) {
        Matcher matcher = MENTION_PATTERN.matcher(message);

        while (matcher.find()) {
            String targetName = matcher.group(1);
            MinecraftServer server = sender.getServer();

            Team team = server.getScoreboard().getTeam(targetName);
            if (team != null) {
                String error = handleTeamMention(sender, team, server);
                if (error != null) {
                    return error;
                }
                continue;
            }

            ServerPlayerEntity target = server.getPlayerManager().getPlayer(targetName);
            if (target == null) continue;

            // Check if the TARGET has mentions enabled
            if (!CustomCommands.areMentionsEnabledFor(target)) continue;

            // Check cooldown on the SENDER
            long now = System.currentTimeMillis();
            Long lastTime = lastMentionTime.get(sender.getUuid());
            if (lastTime != null && (now - lastTime) < MENTION_COOLDOWN_MS) {
                long remainingSeconds = (MENTION_COOLDOWN_MS - (now - lastTime)) / 1000;
                return "You can't mention again for another " + remainingSeconds + " seconds.";
            }

            lastMentionTime.put(sender.getUuid(), now);
            notifyPlayer(target, sender.getName().getString());
        }

        return null;
    }

    private static String handleTeamMention(ServerPlayerEntity sender, Team team, MinecraftServer server) {
        long now = System.currentTimeMillis();
        Long lastTime = lastMentionTime.get(sender.getUuid());
        if (lastTime != null && (now - lastTime) < MENTION_COOLDOWN_MS) {
            long remainingSeconds = (MENTION_COOLDOWN_MS - (now - lastTime)) / 1000;
            return "You can't mention again for another " + remainingSeconds + " seconds.";
        }

        boolean notified = false;
        for (String playerName : team.getPlayerList()) {
            ServerPlayerEntity target = server.getPlayerManager().getPlayer(playerName);
            //if (target == null || target == sender) continue;
            if (!CustomCommands.areMentionsEnabledFor(target)) continue;
            notifyPlayer(target, sender.getName().getString());
            notified = true;
        }

        if (notified) {
            lastMentionTime.put(sender.getUuid(), now);
        }

        return null;
    }

    private static void notifyPlayer(ServerPlayerEntity target, String senderName) {
        Text notification = Text.empty()
                .append(Text.literal("📣 ")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFD700)).withBold(true)))
                .append(Text.literal(senderName)
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF)).withBold(true)))
                .append(Text.literal(" mentioned you!")
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFD700))));

        target.sendMessage(notification, true);

        target.getServerWorld().playSound(
                null,
                target.getX(),
                target.getY(),
                target.getZ(),
                SoundEvents.BLOCK_BELL_USE,
                SoundCategory.PLAYERS,
                1.0f,
                1.0f
        );
    }
}
