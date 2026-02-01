package net.tcmfatbird.tutorialmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.tcmfatbird.tutorialmod.feature.ChatMentions;
import net.tcmfatbird.tutorialmod.network.ClockTogglePacket;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CustomCommands {

    // Tracks per-player mention toggle state (default: on)
    private static final Map<UUID, Boolean> mentionsEnabled = new ConcurrentHashMap<>();
    // Tracks per-player clock toggle state (default: on)
    private static final Map<UUID, Boolean> clockEnabled = new ConcurrentHashMap<>();

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerHelloCommand(dispatcher);
            registerHealCommand(dispatcher);
            registerBoostCommand(dispatcher);
            registerMentionCommand(dispatcher);
            registerClockCommand(dispatcher);
        });
    }

    // ─── PUBLIC HELPERS (used by ChatMentions) ───────────────────────────────

    public static boolean areMentionsEnabledFor(ServerPlayerEntity player) {
        return mentionsEnabled.getOrDefault(player.getUuid(), true);
    }

    public static boolean isClockEnabledFor(ServerPlayerEntity player) {
        return clockEnabled.getOrDefault(player.getUuid(), true);
    }

    // ─── /mention on|off ──────────────────────────────────────────────────────

    private static void registerMentionCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("mention")
                .then(CommandManager.literal("on").executes(ctx -> setMentions(ctx, true)))
                .then(CommandManager.literal("off").executes(ctx -> setMentions(ctx, false))));
    }

    private static int setMentions(CommandContext<ServerCommandSource> context, boolean enabled) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            mentionsEnabled.put(player.getUuid(), enabled);
            context.getSource().sendFeedback(
                    () -> Text.literal("Mentions " + (enabled ? "enabled" : "disabled") + "."), false);
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Only players can use this command!"));
            return 0;
        }
    }

    // ─── /clock on|off ────────────────────────────────────────────────────────

    private static void registerClockCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("clock")
                .then(CommandManager.literal("on").executes(ctx -> setClock(ctx, true)))
                .then(CommandManager.literal("off").executes(ctx -> setClock(ctx, false))));
    }

    private static int setClock(CommandContext<ServerCommandSource> context, boolean enabled) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            clockEnabled.put(player.getUuid(), enabled);

            // Send the toggle state to the client so it can stop/start the clock display
            ServerPlayNetworking.send(player, new ClockTogglePacket(enabled));

            context.getSource().sendFeedback(
                    () -> Text.literal("Clock " + (enabled ? "enabled" : "disabled") + "."), false);
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Only players can use this command!"));
            return 0;
        }
    }

    // ─── /hello ───────────────────────────────────────────────────────────────

    private static void registerHelloCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("hello")
                .executes(CustomCommands::executeHello));
    }

    private static int executeHello(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(
                () -> Text.literal("Hello from your custom mod! Welcome to Minecraft modding!"),
                false
        );
        return 1;
    }

    // ─── /heal ────────────────────────────────────────────────────────────────

    private static void registerHealCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("heal")
                .executes(CustomCommands::executeHealFull)
                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1, 20))
                        .executes(CustomCommands::executeHealAmount)));
    }

    private static int executeHealFull(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            player.setHealth(player.getMaxHealth());

            player.getWorld().playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_PLAYER_LEVELUP,
                    SoundCategory.PLAYERS,
                    1.0F, 1.0F
            );

            context.getSource().sendFeedback(
                    () -> Text.literal("Fully healed!"),
                    false
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Only players can use this command!"));
            return 0;
        }
    }

    private static int executeHealAmount(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            int amount = IntegerArgumentType.getInteger(context, "amount");

            float currentHealth = player.getHealth();
            float newHealth = Math.min(currentHealth + amount, player.getMaxHealth());
            player.setHealth(newHealth);

            player.getWorld().playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                    SoundCategory.PLAYERS,
                    0.5F, 1.0F
            );

            context.getSource().sendFeedback(
                    () -> Text.literal("Healed for " + amount + " health!"),
                    false
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Only players can use this command!"));
            return 0;
        }
    }

    // ─── /boost ───────────────────────────────────────────────────────────────

    private static void registerBoostCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("boost")
                .then(CommandManager.argument("effect", StringArgumentType.word())
                        .executes(CustomCommands::executeBoostDefault)
                        .then(CommandManager.argument("duration", IntegerArgumentType.integer(1, 600))
                                .executes(CustomCommands::executeBoostDuration))));
    }

    private static int executeBoostDefault(CommandContext<ServerCommandSource> context) {
        return executeBoost(context, 10);
    }

    private static int executeBoostDuration(CommandContext<ServerCommandSource> context) {
        int duration = IntegerArgumentType.getInteger(context, "duration");
        return executeBoost(context, duration);
    }

    private static int executeBoost(CommandContext<ServerCommandSource> context, int durationSeconds) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            String effectName = StringArgumentType.getString(context, "effect").toLowerCase();
            int durationTicks = durationSeconds * 20;

            StatusEffectInstance effect = switch (effectName) {
                case "speed" -> new StatusEffectInstance(StatusEffects.SPEED, durationTicks, 1);
                case "jump" -> new StatusEffectInstance(StatusEffects.JUMP_BOOST, durationTicks, 1);
                case "strength" -> new StatusEffectInstance(StatusEffects.STRENGTH, durationTicks, 1);
                case "regen" -> new StatusEffectInstance(StatusEffects.REGENERATION, durationTicks, 1);
                case "night_vision" -> new StatusEffectInstance(StatusEffects.NIGHT_VISION, durationTicks, 0);
                case "fire_resistance" -> new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, durationTicks, 0);
                case "water_breathing" -> new StatusEffectInstance(StatusEffects.WATER_BREATHING, durationTicks, 0);
                default -> null;
            };

            if (effect == null) {
                context.getSource().sendError(
                        Text.literal("Unknown effect: " + effectName +
                                ". Available: speed, jump, strength, regen, night_vision, fire_resistance, water_breathing")
                );
                return 0;
            }

            player.addStatusEffect(effect);

            player.getWorld().playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_PLAYER_LEVELUP,
                    SoundCategory.PLAYERS,
                    0.5F, 2.0F
            );

            context.getSource().sendFeedback(
                    () -> Text.literal("Applied " + effectName + " for " + durationSeconds + " seconds!"),
                    false
            );
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Only players can use this command!"));
            return 0;
        }
    }
}