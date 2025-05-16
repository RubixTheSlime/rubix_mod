package io.github.rubixtheslime.rubix.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.rubixtheslime.rubix.client.RubixModClient;
import io.github.rubixtheslime.rubix.imixin.client.IMixinVertexConsumer;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FluidRenderer.class)
public class MixinFluidRenderer {

    @Inject(method = "render", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/BlockRenderView;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
    void renderSetBaseColor(
        BlockRenderView world,
        BlockPos pos,
        VertexConsumer vertexConsumer,
        BlockState blockState,
        FluidState fluidState,
        CallbackInfo ci,
        @Local boolean bl,
        @Local int i
    ) {
        int color = !bl && RubixModClient.prideFlagManager.isAnimated(pos) ? i | 0xff00_0000 : 0;
        ((IMixinVertexConsumer) vertexConsumer).rubix$getDeferrer().setBaseColor(color);
    }

    @Inject(method = "vertex", at = @At("HEAD"), cancellable = true)
    void vertex(VertexConsumer vertexConsumer, float x, float y, float z, float red, float green, float blue, float u, float v, int light, CallbackInfo ci) {
        var deferrer = ((IMixinVertexConsumer)vertexConsumer).rubix$getDeferrer();
        if (deferrer.isAnimating()) {
            deferrer.vertex(x, y, z).color(red, green, blue, 1.0F).texture(u, v).light(light).normal(0.0F, 1.0F, 0.0F);
            ci.cancel();
        }
    }


}
