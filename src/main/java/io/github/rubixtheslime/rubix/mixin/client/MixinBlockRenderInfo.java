package io.github.rubixtheslime.rubix.mixin.client;

import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockRenderInfo.class)
public class MixinBlockRenderInfo {

//    @Inject(method = "blockColor", at = @At("HEAD"), cancellable = true)
//    public void blockColor(int tintIndex, CallbackInfoReturnable<Integer> cir) {
//        cir.setReturnValue(-1);
//    }

}
