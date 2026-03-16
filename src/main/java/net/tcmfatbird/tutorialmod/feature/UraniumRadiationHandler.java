package net.tcmfatbird.tutorialmod.feature;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.tcmfatbird.tutorialmod.block.ModBlocks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class UraniumRadiationHandler {
    private static final int CHECK_RADIUS = 3;
    private static final int EXPOSURE_THRESHOLD_TICKS = 100;
    private static final int MAX_EXPOSURE = 400;

    private static final Map<UUID, Integer> exposureTicks = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(UraniumRadiationHandler::onWorldTick);
    }

    private static void onWorldTick(ServerWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            UUID uuid = player.getUuid();
            int exposure = exposureTicks.getOrDefault(uuid, 0);

            if (isNearUraniumOre(world, player.getBlockPos())) {
                exposure = Math.min(MAX_EXPOSURE, exposure + 1);
            } else {
                exposure = Math.max(0, exposure - 2);
            }

            if (exposure >= EXPOSURE_THRESHOLD_TICKS) {
                applyRadiationEffects(player, exposure);
            }

            if (exposure == 0) {
                exposureTicks.remove(uuid);
            } else {
                exposureTicks.put(uuid, exposure);
            }
        }

        cleanupDisconnectedPlayers(world);
    }

    private static boolean isNearUraniumOre(ServerWorld world, BlockPos center) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();

        for (int x = -CHECK_RADIUS; x <= CHECK_RADIUS; x++) {
            for (int y = -CHECK_RADIUS; y <= CHECK_RADIUS; y++) {
                for (int z = -CHECK_RADIUS; z <= CHECK_RADIUS; z++) {
                    mutablePos.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    Block block = world.getBlockState(mutablePos).getBlock();

                    if (block == ModBlocks.URANIUM_ORE || block == ModBlocks.URANIUM_DEEPSLATE_ORE) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static void applyRadiationEffects(ServerPlayerEntity player, int exposure) {
        int amplifier = exposure >= 240 ? 1 : 0;

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, amplifier, true, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 80, amplifier, true, true));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 80, 0, true, true));

        if (player.age % 40 == 0) {
            player.damage(player.getDamageSources().magic(), 1.0f);
        }
    }

    private static void cleanupDisconnectedPlayers(ServerWorld world) {
        Iterator<UUID> iterator = exposureTicks.keySet().iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            if (world.getServer().getPlayerManager().getPlayer(uuid) == null) {
                iterator.remove();
            }
        }
    }
}
