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
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Thickened Ink Item - Makes text on signs bold when right-clicked
 * This item can be used on any type of sign (standing, wall, or hanging) to make
 * all the text on the sign bold. The item will be consumed when used successfully.
 * If the text is already bold, the item will not be consumed.
 * Note: This class will show as "unused" until it's registered in the mod's item registry.
 * This is expected behavior during development.
 */
public class ThickenedInkItem extends Item {

    /**
     * Constructor for the Thickened Ink Item
     * Note: This constructor will show as "unused" until the item is registered
     * in the mod's initialization code. This is expected behavior.
     */
    public ThickenedInkItem(Settings settings) {
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
        SignText newFront = applyBoldFormatting(front);
        if (newFront != front) {
            signEntity.setText(newFront, true);
            madeChanges = true;
        }

// BACK
        SignText back = signEntity.getBackText();
        SignText newBack = applyBoldFormatting(back);
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
                SoundEvents.ITEM_GLOW_INK_SAC_USE,
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
     * Applies bold formatting to all lines of text in a SignText.
     * Returns the original SignText if all lines are already bold.
     *
     * @param signText The original SignText to process
     * @return A new SignText with bold formatting, or the original if already bold
     */
    private SignText applyBoldFormatting(SignText signText) {
        boolean anyNeedsBold = false;
        SignText result = signText;

        // Process each of the 4 lines of text on the sign
        for (int lineIndex = 0; lineIndex < 4; lineIndex++) {
            // Get the text for this line (without filtering)
            Text originalText = signText.getMessage(lineIndex, false);

            // Check if this line's text needs to be made bold
            if (isTextNotBold(originalText)) {
                anyNeedsBold = true;

                // Create a new Text with bold formatting
                Text boldText = originalText.copy().setStyle(
                        originalText.getStyle().withBold(true)
                );

                // Update the SignText with the new bold text for this line
                result = result.withMessage(lineIndex, boldText);
            }
        }

        // If no lines needed bolding (all were already bold), return the original SignText
        // This signals that no changes were made
        if (!anyNeedsBold) {
            return signText;
        }

        return result;
    }

    /**
     * Recursively checks if a Text component or any of its siblings are NOT bold
     *
     * @param text The Text to check
     * @return true if the text or any of its siblings are not bold
     */
    private boolean isTextNotBold(Text text) {
        // Check if the current text component is not bold
        if (!text.getStyle().isBold()) {
            return true;
        }

        // Check all sibling text components
        for (Text sibling : text.getSiblings()) {
            if (isTextNotBold(sibling)) {
                return true;
            }
        }

        return false;
    }
}
