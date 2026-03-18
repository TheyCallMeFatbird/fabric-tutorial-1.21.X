package net.tcmfatbird.tutorialmod.item.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class QuantumTunnelerItem extends Item {

    private static final int MAX_TUNNEL_THICKNESS = 2;
    private static final int MAX_REACH = 6;

    public QuantumTunnelerItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient()) return TypedActionResult.pass(user.getStackInHand(hand));

        Vec3d look = user.getRotationVec(1.0f).normalize();
        BlockPos playerPos = user.getBlockPos();

        int wallStart = -1;
        int wallEnd = -1;

        // Scan forward up to MAX_REACH blocks
        for (int i = 1; i <= MAX_REACH; i++) {
            BlockPos check = playerPos.add(
                    (int) Math.round(look.x * i),
                    0,
                    (int) Math.round(look.z * i)
            );

            boolean isSolid = !world.getBlockState(check).isAir();

            if (isSolid && wallStart == -1) {
                wallStart = i;
            } else if (!isSolid && wallStart != -1) {
                wallEnd = i;
                break;
            }
        }

        // Check wall exists and is thin enough
        if (wallStart == -1 || wallEnd == -1) {
            user.sendMessage(net.minecraft.text.Text.literal("§cNo wall to tunnel through!"), true);
            return TypedActionResult.fail(user.getStackInHand(hand));
        }

        int thickness = wallEnd - wallStart;
        if (thickness > MAX_TUNNEL_THICKNESS) {
            user.sendMessage(net.minecraft.text.Text.literal("§cWall is too thick!"), true);
            return TypedActionResult.fail(user.getStackInHand(hand));
        }

        // Teleport to the other side
        BlockPos destination = playerPos.add(
                (int) Math.round(look.x * wallEnd),
                0,
                (int) Math.round(look.z * wallEnd)
        );

        // Make sure destination is safe (not inside a block)
        if (!world.getBlockState(destination).isAir() ||
                !world.getBlockState(destination.up()).isAir()) {
            user.sendMessage(net.minecraft.text.Text.literal("§cCan't tunnel here!"), true);
            return TypedActionResult.fail(user.getStackInHand(hand));
        }

        // Do the teleport
        user.teleport(
                destination.getX() + 0.5,
                destination.getY(),
                destination.getZ() + 0.5,
                true
        );

        // Sound + particle effect
        world.playSound(null,
                destination.getX(), destination.getY(), destination.getZ(),
                SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                SoundCategory.PLAYERS,
                1.0f, 1.8f
        );

        // Damage the item
        if (user instanceof ServerPlayerEntity serverPlayer) {
            user.getStackInHand(hand).damage(1, serverPlayer, net.minecraft.entity.EquipmentSlot.MAINHAND);
        }

        return TypedActionResult.success(user.getStackInHand(hand));
    }
}