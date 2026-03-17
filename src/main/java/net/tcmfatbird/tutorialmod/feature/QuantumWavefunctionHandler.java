package net.tcmfatbird.tutorialmod.feature;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.tcmfatbird.tutorialmod.block.ModBlocks;
import net.tcmfatbird.tutorialmod.item.ModItems;

public final class QuantumWavefunctionHandler {
    private QuantumWavefunctionHandler() {}

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                return true;
            }

            if (!isWearingGoggles(serverPlayer)) {
                return true;
            }

            float chance = getCollapseChance(state, pos.getY());
            if (chance <= 0f) {
                return true;
            }

            world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.BLOCKS, 0.35f, 1.5f);

            if (positionRoll(pos) < chance) {
                world.setBlockState(pos, getCollapsedOre(state, pos), Block.NOTIFY_ALL);
            }

            return true;
        });
    }

    public static float getCollapseChance(BlockState state, int y) {
        if (state.isOf(Blocks.STONE)) {
            return y < 24 ? 0.18f : 0.12f;
        }

        if (state.isOf(Blocks.DEEPSLATE)) {
            return y < -24 ? 0.3f : 0.2f;
        }

        return 0f;
    }

    public static float positionRoll(BlockPos pos) {
        int hash = pos.getX() * 73428767 ^ pos.getY() * 9127837 ^ pos.getZ() * 43828921;
        hash ^= (hash >>> 13);
        return (hash & 0x7fffffff) / (float) Integer.MAX_VALUE;
    }

    private static boolean isWearingGoggles(ServerPlayerEntity player) {
        return player.getEquippedStack(EquipmentSlot.HEAD).isOf(ModItems.WAVEFUNCTION_GOGGLES);
    }

    private static Block getCollapsedOre(BlockState state, BlockPos pos) {
        int chooser = Math.floorMod(pos.getX() * 31 + pos.getY() * 17 + pos.getZ() * 13, 2);

        if (state.isOf(Blocks.STONE)) {
            return (pos.getY() < 0 && chooser == 1) ? ModBlocks.URANIUM_ORE : ModBlocks.PINK_GARNET_ORE;
        }

        if (state.isOf(Blocks.DEEPSLATE)) {
            return (pos.getY() < -16 && chooser == 1) ? ModBlocks.URANIUM_DEEPSLATE_ORE : ModBlocks.PINK_GARNET_DEEPSLATE_ORE;
        }

        return Blocks.AIR;
    }
}
