package net.tcmfatbird.tutorialmod.item.custom;

import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class XpOrbSphereItem extends Item {

    public XpOrbSphereItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient()) {
            net.minecraft.client.MinecraftClient.getInstance()
                    .setScreen(new XpOrbSphereScreen(user.getStackInHand(hand)));
            return TypedActionResult.success(user.getStackInHand(hand));
        }
        return TypedActionResult.pass(user.getStackInHand(hand));
    }

    public static void spawnSphere(World world, PlayerEntity user, int orbCount, float radius) {
        double cx = user.getX();
        double cy = user.getY() + 1.0;
        double cz = user.getZ();

        double goldenRatio = (1.0 + Math.sqrt(5.0)) / 2.0;

        for (int i = 0; i < orbCount; i++) {
            double theta = 2.0 * Math.PI * i / goldenRatio;
            double phi = Math.acos(1.0 - 2.0 * (i + 0.5) / orbCount);

            double x = cx + radius * Math.sin(phi) * Math.cos(theta);
            double y = cy + radius * Math.cos(phi);
            double z = cz + radius * Math.sin(phi) * Math.sin(theta);

            ExperienceOrbEntity orb = new ExperienceOrbEntity(world, x, y, z, 1);
            orb.setVelocity(0, 0, 0);
            orb.setNoGravity(true);
            world.spawnEntity(orb);
        }

        world.playSound(null, cx, cy, cz,
                SoundEvents.ENTITY_PLAYER_LEVELUP,
                SoundCategory.PLAYERS, 1.0f, 0.8f);

        for (int i = 0; i < 30; i++) {
            double ox = (world.random.nextDouble() - 0.5) * radius * 2;
            double oy = (world.random.nextDouble() - 0.5) * radius * 2;
            double oz = (world.random.nextDouble() - 0.5) * radius * 2;
            world.addParticle(ParticleTypes.ENCHANT, cx + ox, cy + oy, cz + oz, 0, 0.05, 0);
        }
    }
}