package io.github.rubixtheslime.rubix.mixin.client;

import io.github.rubixtheslime.rubix.render.DynColorBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockColors.class)
public class MixinBlockColors {

    @Inject(method = "getColor", at = @At("HEAD"), cancellable = true)
    public void getColor(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex, CallbackInfoReturnable<Integer> cir) {
//        if (DynColorBuilder.isGreenScreening()) cir.setReturnValue(0xff00ff00);
    }
}
