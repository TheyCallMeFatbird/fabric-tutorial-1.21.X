package net.tcmfatbird.tutorialmod.mixin;

import net.minecraft.block.entity.SculkCatalystBlockEntity;
import net.minecraft.block.entity.SculkSpreadManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SculkCatalystBlockEntity.Listener.class)
public class SculkSpreaderMixin {

    private static final int XP_MULTIPLIER = 999999999; // change this

    @Redirect(
            method = "listen",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/entity/SculkSpreadManager;spread(Lnet/minecraft/util/math/BlockPos;I)V"
            )
    )
    private void tutorialmod$boostSculkSpread(
            SculkSpreadManager spreadManager,
            BlockPos pos,
            int charge
    ) {
        spreadManager.spread(pos, charge * XP_MULTIPLIER);
    }
}
