package io.github.rubixtheslime.rubix.mixin.client;

import io.github.rubixtheslime.rubix.imixin.client.IMixinVertexConsumer;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BlockRenderManager.class)
public class MixinBlockRenderManager {

    @Shadow @Final private BlockColors blockColors;

//    @Inject(method = "renderBlock", at = @At("HEAD"))
//    public void renderBlock(BlockState state, BlockPos pos, BlockRenderView world, MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull, List<BlockModelPart> parts, CallbackInfo ci) {
//        ((IMixinVertexConsumer) vertexConsumer).rubix$block(pos, state, blockColors);
//    }

//    @Inject(method = "renderFluid", at = @At("HEAD"))
//    public void renderFluid(BlockPos pos, BlockRenderView world, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
//        ((IMixinVertexConsumer) vertexConsumer).rubix$block(pos, blockState, blockColors);
//    }

}
