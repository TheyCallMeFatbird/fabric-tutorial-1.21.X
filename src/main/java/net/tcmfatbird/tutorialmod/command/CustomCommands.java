package net.tcmfatbird.tutorialmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class CustomCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // Simple hello command
            registerHelloCommand(dispatcher);

            // Command with arguments
            registerHealCommand(dispatcher);

            // Command with player effects
            registerBoostCommand(dispatcher);
        });
    }

    /**
     * Simple command: /hello
     * Sends a greeting message to the player
     */
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

    /**
     * Command with arguments: /heal [amount]
     * Heals the player by the specified amount (default: full health)
     */
    private static void registerHealCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("heal")
                // Without arguments - full heal
                .executes(CustomCommands::executeHealFull)
                // With amount argument
                .then(CommandManager.argument("amount", IntegerArgumentType.integer(1, 20))
                        .executes(CustomCommands::executeHealAmount)));
    }

    private static int executeHealFull(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            player.setHealth(player.getMaxHealth());

            // Play healing sound
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

            // Play healing sound
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

    /**
     * Command: /boost <effect> [duration]
     * Gives the player a potion effect boost
     * Examples: /boost speed, /boost jump 30
     */
    private static void registerBoostCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("boost")
                .then(CommandManager.argument("effect", StringArgumentType.word())
                        // Without duration - default 10 seconds
                        .executes(CustomCommands::executeBoostDefault)
                        // With duration argument (in seconds)
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
            int durationTicks = durationSeconds * 20; // Convert seconds to ticks

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

            // Play effect sound
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