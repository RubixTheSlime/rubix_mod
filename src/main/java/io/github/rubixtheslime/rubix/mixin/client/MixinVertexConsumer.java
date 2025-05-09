package io.github.rubixtheslime.rubix.mixin.client;

import io.github.rubixtheslime.rubix.RDebug;
import io.github.rubixtheslime.rubix.imixin.client.IMixinVertexConsumer;
import io.github.rubixtheslime.rubix.render.DynColorData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VertexConsumer.class)
public interface MixinVertexConsumer {

//    @Inject(method = "vertex(FFF)Lnet/minecraft/client/render/VertexConsumer;", at = @At("HEAD"), cancellable = true)
//    default void vertex(float par1, float par2, float par3, CallbackInfoReturnable<VertexConsumer> cir) {
//
//    }
//
//    @Inject(method = "quad(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/model/BakedQuad;FFFFII)V", at = @At("HEAD"), cancellable = true)
//    default void quad(MatrixStack.Entry matrixEntry, BakedQuad quad, float red, float green, float blue, float f, int i, int j, CallbackInfo ci) {
//        if (!rubix$shouldDelay()) return;
//        if (RDebug.b1() && !quad.hasTint()) return;
//        rubix$queueQuad(matrixEntry, quad, i, j);
//        ci.cancel();
//    }
}
