package net.tcmfatbird.tutorialmod.item.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.Comparator;
import java.util.List;

public class StarlightAshesItem extends Item {
    private static final double STARFALL_RADIUS = 16.0;
    private static final int MAX_TARGETS = 5;

    public StarlightAshesItem(Settings settings) {
        super(settings);
    }


    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("tooltip.tutorialmod.starlight_ashes"));
        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient()) {
            return TypedActionResult.success(stack);
        }

        ServerWorld serverWorld = (ServerWorld) world;
        boolean isNight = !world.isDay();
        boolean canSeeSky = world.isSkyVisible(user.getBlockPos());

        if (!isNight && !canSeeSky) {
            user.sendMessage(Text.literal("§7The ashes only awaken beneath the open night sky."), true);
            return TypedActionResult.fail(stack);
        }

        summonStarfall(serverWorld, user);
        empowerPlayer(user);
        int struckTargets = strikeNearbyHostiles(serverWorld, user);

        if (!user.isCreative()) {
            stack.decrement(1);
        }

        user.getItemCooldownManager().set(this, 20 * 12);
        user.sendMessage(Text.literal(struckTargets > 0
                ? "§bStarlight crashes down on " + struckTargets + " nearby foes!"
                : "§bThe ashes wrap you in falling starlight."), true);

        return TypedActionResult.success(stack);
    }

    private void summonStarfall(ServerWorld world, PlayerEntity user) {
        Vec3d center = user.getPos().add(0.0, 1.0, 0.0);

        for (int i = 0; i < 60; i++) {
            double angle = (Math.PI * 2.0 * i) / 60.0;
            double radius = 2.5 + (i % 6) * 0.35;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + 3.0 + (i % 5) * 0.4;

            world.spawnParticles(ParticleTypes.END_ROD, x, y, z, 1, 0.0, -0.3, 0.0, 0.02);
            world.spawnParticles(ParticleTypes.WITCH, x, y, z, 1, 0.08, 0.08, 0.08, 0.0);
        }

        world.spawnParticles(ParticleTypes.GLOW, center.x, center.y + 1.5, center.z,
                40, 0.8, 1.0, 0.8, 0.02);
        world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM,
                SoundCategory.PLAYERS, 1.0f, 0.7f);
        world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR,
                SoundCategory.PLAYERS, 1.0f, 0.6f);
    }

    private void empowerPlayer(PlayerEntity user) {
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 20 * 15, 0));
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 20 * 15, 1));
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 20 * 30, 0));
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 20 * 8, 0));
    }

    private int strikeNearbyHostiles(ServerWorld world, PlayerEntity user) {
        Box searchBox = user.getBoundingBox().expand(STARFALL_RADIUS);
        List<HostileEntity> hostiles = world.getEntitiesByClass(HostileEntity.class, searchBox,
                        entity -> entity.isAlive() && entity.squaredDistanceTo(user) <= STARFALL_RADIUS * STARFALL_RADIUS)
                .stream()
                .sorted(Comparator.comparingDouble(entity -> entity.squaredDistanceTo(user)))
                .limit(MAX_TARGETS)
                .toList();

        for (HostileEntity hostile : hostiles) {
            rainStarOnTarget(world, user, hostile);
        }

        return hostiles.size();
    }

    private void rainStarOnTarget(ServerWorld world, PlayerEntity user, LivingEntity target) {
        Vec3d pos = target.getPos().add(0.0, target.getHeight() * 0.5, 0.0);
        world.spawnParticles(ParticleTypes.END_ROD, pos.x, pos.y + 3.5, pos.z,
                20, 0.25, 1.2, 0.25, 0.02);
        world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y + 0.5, pos.z,
                12, 0.2, 0.4, 0.2, 0.03);
        world.spawnParticles(ParticleTypes.GLOW, pos.x, pos.y, pos.z,
                16, 0.35, 0.6, 0.35, 0.02);

        target.timeUntilRegen = 0;
        target.damage(world, user.getDamageSources().magic(), 8.0f);
        target.setOnFireFor(2);
        target.addVelocity(0.0, 0.45, 0.0);
        target.velocityModified = true;

        world.playSound(null, target.getBlockPos(), SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST,
                SoundCategory.PLAYERS, 0.8f, 1.6f);
    }
}
