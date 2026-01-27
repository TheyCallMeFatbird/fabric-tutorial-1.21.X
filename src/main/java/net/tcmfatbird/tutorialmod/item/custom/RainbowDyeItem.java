package net.tcmfatbird.tutorialmod.item.custom;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.text.MutableText; // <--- ADDED THIS IMPORT
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Rainbow Dye Item - Makes text on signs rainbow colored when right-clicked
 * This applies a static rainbow gradient to the text on the sign.
 * The item will be consumed when used successfully.
 */
public class RainbowDyeItem extends Item {

    /**
     * Constructor for the Rainbow Dye Item
     */
    public RainbowDyeItem(Settings settings) {
        super(settings);
    }

    /**
     * Called when the player uses this item on a block
     *
     * @param context Contains information about the usage context (world, position, player, etc.)
     * @return ActionResult indicating whether the action was successful
     */
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();

        // Get the block at the clicked position
        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();

        // Check if the block is any type of sign
        if (!isSign(block)) {
            return ActionResult.PASS;
        }

        // Get the sign's block entity (contains the text data)
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof SignBlockEntity signEntity)) {
            return ActionResult.PASS;
        }

        // Track whether we made any changes to the sign
        boolean madeChanges = false;

// FRONT
        SignText front = signEntity.getFrontText();
        SignText newFront = applyRainbowFormatting(front);
        if (newFront != front) {
            signEntity.setText(newFront, true);
            madeChanges = true;
        }

// BACK
        SignText back = signEntity.getBackText();
        SignText newBack = applyRainbowFormatting(back);
        if (newBack != back) {
            signEntity.setText(newBack, false);
            madeChanges = true;
        }

        if (!madeChanges) {
            return ActionResult.PASS;
        }

        // Mark the block entity as dirty to ensure changes are saved
        signEntity.markDirty();

        // Update the block on the client side to reflect changes immediately
        if (!world.isClient()) {
            world.updateListeners(pos, blockState, blockState, Block.NOTIFY_ALL);
        }

        // Play the glow ink sac sound effect
        world.playSound(
                player,
                pos,
                SoundEvents.ITEM_DYE_USE, // Changed to Dye sound
                SoundCategory.BLOCKS,
                1.0f, // Volume
                1.0f  // Pitch
        );

        // Consume the item (decrease stack count by 1)
        // Don't consume in creative mode
        if (player != null && !player.isCreative()) {
            stack.decrement(1);
        }

        // Return appropriate result based on whether we're on client or server
        return world.isClient() ? ActionResult.SUCCESS : ActionResult.CONSUME;
    }

    /**
     * Checks if a block is any type of sign
     *
     * @param block The block to check
     * @return true if the block is a sign (standing, wall, or hanging)
     */
    private boolean isSign(Block block) {
        return block instanceof SignBlock
                || block instanceof WallSignBlock
                || block instanceof HangingSignBlock
                || block instanceof WallHangingSignBlock;
    }

    /**
     * Applies rainbow formatting to all lines of text in a SignText.
     * Returns the original SignText if the lines are empty.
     *
     * @param signText The original SignText to process
     * @return A new SignText with rainbow formatting, or the original if empty
     */
    private SignText applyRainbowFormatting(SignText signText) {
        boolean anyNeedsRainbow = false;
        SignText result = signText;

        // Define the colors for the rainbow gradient
        Formatting[] rainbowColors = {
                Formatting.RED, Formatting.GOLD, Formatting.YELLOW, Formatting.GREEN,
                Formatting.AQUA, Formatting.BLUE, Formatting.LIGHT_PURPLE
        };

        // Process each of the 4 lines of text on the sign
        for (int lineIndex = 0; lineIndex < 4; lineIndex++) {
            // Get the text for this line
            Text originalText = signText.getMessage(lineIndex, false);
            String content = originalText.getString();

            if (!content.isEmpty()) {
                anyNeedsRainbow = true;

                // FIX: Use MutableText here instead of Text, so we can use .append()
                MutableText rainbowLine = Text.empty();

                for (int i = 0; i < content.length(); i++) {
                    char c = content.charAt(i);
                    // Cycle through colors
                    Formatting color = rainbowColors[i % rainbowColors.length];
                    // Append the character with the specific color
                    rainbowLine.append(Text.literal(String.valueOf(c)).formatted(color));
                }

                // Update the SignText with the new rainbow text for this line
                result = result.withMessage(lineIndex, rainbowLine);
            }
        }

        // If no lines needed rainbowing (all were empty), return the original SignText
        if (!anyNeedsRainbow) {
            return signText;
        }

        return result;
    }
}
