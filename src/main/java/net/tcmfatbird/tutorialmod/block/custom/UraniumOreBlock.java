package net.tcmfatbird.tutorialmod.block.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class UraniumOreBlock extends ExperienceDroppingBlock {
    public UraniumOreBlock(IntProvider experienceDropped, Settings settings) {
        super(experienceDropped, settings);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(4) != 0) {
            return;
        }

        for (Direction direction : Direction.values()) {
            BlockPos offsetPos = pos.offset(direction);
            if (world.getBlockState(offsetPos).isOpaqueFullCube(world, offsetPos)) {
                continue;
            }

            Direction.Axis axis = direction.getAxis();
            double particleX = axis == Direction.Axis.X ? pos.getX() + 0.5 + 0.55 * direction.getOffsetX() : pos.getX() + random.nextDouble();
            double particleY = axis == Direction.Axis.Y ? pos.getY() + 0.5 + 0.55 * direction.getOffsetY() : pos.getY() + random.nextDouble();
            double particleZ = axis == Direction.Axis.Z ? pos.getZ() + 0.5 + 0.55 * direction.getOffsetZ() : pos.getZ() + random.nextDouble();

            world.addParticle(ParticleTypes.HAPPY_VILLAGER, particleX, particleY, particleZ, 0.0, 0.01, 0.0);
        }
    }
}