package net.tcmfatbird.tutorialmod.feature;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.tcmfatbird.tutorialmod.item.ModItems;
import net.tcmfatbird.tutorialmod.network.TemporalRewindStatePacket;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TemporalRewindManager {
    private static final int MAX_POSITION_HISTORY = 20 * 60 * 5;

    private static final Map<UUID, PlayerTimeline> TIMELINES = new HashMap<>();

    private TemporalRewindManager() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                PlayerTimeline timeline = TIMELINES.computeIfAbsent(player.getUuid(), id -> new PlayerTimeline());

                if (timeline.rewinding) {
                    rewindOneTick(player, timeline);
                } else {
                    captureTick(player, timeline);
                }
            }
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                return true;
            }

            PlayerTimeline timeline = TIMELINES.computeIfAbsent(serverPlayer.getUuid(), id -> new PlayerTimeline());
            if (!timeline.rewinding && hasRewindDevice(serverPlayer)) {
                timeline.brokenBlocks.addLast(new BrokenBlock(pos.toImmutable(), state));
            }
            return true;
        });
    }

    public static void setRewinding(ServerPlayerEntity player, boolean rewinding) {
        PlayerTimeline timeline = TIMELINES.computeIfAbsent(player.getUuid(), id -> new PlayerTimeline());
        if (!hasRewindDevice(player)) {
            setRewindingState(player, timeline, false);
            return;
        }

        setRewindingState(player, timeline, rewinding);
    }

    private static void setRewindingState(ServerPlayerEntity player, PlayerTimeline timeline, boolean rewinding) {
        if (timeline.rewinding == rewinding) {
            return;
        }

        timeline.rewinding = rewinding;
        ServerPlayNetworking.send(player, new TemporalRewindStatePacket(rewinding));
    }

    private static void captureTick(ServerPlayerEntity player, PlayerTimeline timeline) {
        if (!hasRewindDevice(player)) {
            timeline.positions.clear();
            timeline.brokenBlocks.clear();
            setRewindingState(player, timeline, false);
            return;
        }

        timeline.positions.addLast(new PlayerSnapshot(
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYaw(),
                player.getPitch(),
                player.getHealth(),
                player.getServerWorld().getTimeOfDay()
        ));

        while (timeline.positions.size() > MAX_POSITION_HISTORY) {
            timeline.positions.removeFirst();
        }
    }

    private static void rewindOneTick(ServerPlayerEntity player, PlayerTimeline timeline) {
        if (!hasRewindDevice(player)) {
            setRewindingState(player, timeline, false);
            return;
        }

        PlayerSnapshot snapshot = timeline.positions.pollLast();
        if (snapshot != null) {
            ServerWorld world = player.getServerWorld();
            player.teleport(world, snapshot.x, snapshot.y, snapshot.z, snapshot.yaw, snapshot.pitch);
            world.setTimeOfDay(snapshot.timeOfDay);
            player.setHealth(Math.min(player.getMaxHealth(), snapshot.health));
        }

        BrokenBlock broken = timeline.brokenBlocks.pollLast();
        if (broken != null && player.getServerWorld().getBlockState(broken.pos()).isAir()) {
            if (consumeRequiredBlockItem(player, broken.state())) {
                player.getServerWorld().setBlockState(broken.pos(), broken.state(), 3);
            }
        }

        if (timeline.positions.isEmpty() && timeline.brokenBlocks.isEmpty()) {
            setRewindingState(player, timeline, false);
        }
    }

    private static boolean consumeRequiredBlockItem(ServerPlayerEntity player, BlockState state) {
        Item requiredItem = state.getBlock().asItem();
        if (requiredItem == Items.AIR) {
            return false;
        }

        for (int slot = 0; slot < player.getInventory().size(); slot++) {
            ItemStack stack = player.getInventory().getStack(slot);
            if (stack.isOf(requiredItem)) {
                stack.decrement(1);
                return true;
            }
        }

        return false;
    }

    private static boolean hasRewindDevice(ServerPlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i).isOf(ModItems.TEMPORAL_REWINDER)) {
                return true;
            }
        }
        return false;
    }

    private record PlayerSnapshot(double x, double y, double z, float yaw, float pitch, float health, long timeOfDay) {}

    private record BrokenBlock(BlockPos pos, BlockState state) {}

    private static final class PlayerTimeline {
        private final Deque<PlayerSnapshot> positions = new ArrayDeque<>();
        private final Deque<BrokenBlock> brokenBlocks = new ArrayDeque<>();
        private boolean rewinding;
    }
}
