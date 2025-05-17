package io.github.rubixtheslime.rubix.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.rubixtheslime.rubix.EnabledMods;
import io.github.rubixtheslime.rubix.client.RubixModClient;
import io.github.rubixtheslime.rubix.imixin.client.IMixinVertexConsumer;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(BlockModelRenderer.class)
public class MixinBlockModelRenderer {
    @Inject(method = "renderQuad", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumer;quad(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/model/BakedQuad;[FFFFF[IIZ)V"), cancellable = true)
    void renderQuad(
        BlockRenderView world,
        BlockState state,
        BlockPos pos,
        VertexConsumer vertexConsumer,
        MatrixStack.Entry matrixEntry,
        BakedQuad quad,
        BlockModelRenderer.LightmapCache lightmap,
        int i,
        CallbackInfo ci,
        @Local(ordinal = 0) float f,
        @Local(ordinal = 1) float g,
        @Local(ordinal = 2) float h
    ) {
        if (EnabledMods.GAY_GRASS_VIDEO && quad.hasTint() && RubixModClient.prideFlagManager.isAnimated(pos)) {
            var deferrer = ((IMixinVertexConsumer) vertexConsumer).rubix$getDeferrer();
            deferrer.setBaseColor(lightmap.field_58165);
            deferrer.quad(matrixEntry, quad, lightmap.fs, f, g, h, 1.0F, lightmap.is, i, true);
            ci.cancel();
        }
    }
}
