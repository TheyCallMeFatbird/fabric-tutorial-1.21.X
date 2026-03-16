package net.tcmfatbird.tutorialmod.mixin;

import net.minecraft.block.entity.SculkSpreadManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SculkSpreadManager.Cursor.class)
public class SculkSpreadManagerCursorMixin {

    @Inject(
            method = "canSpreadTo(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tutorialmod$allowAllNonAir(WorldAccess world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(!world.getBlockState(pos).isAir());
    }
}
