package io.github.rubixtheslime.rubix.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.systems.VertexSorter;
import io.github.rubixtheslime.rubix.EnabledMods;
import io.github.rubixtheslime.rubix.imixin.client.IMixinSectionBuilder;
import io.github.rubixtheslime.rubix.imixin.client.IMixinVertexConsumer;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(value = SectionBuilder.class, priority = 500)
public abstract class MixinSectionBuilder implements IMixinSectionBuilder {

    @Shadow protected abstract BufferBuilder beginBufferBuilding(Map<RenderLayer, BufferBuilder> builders, BlockBufferAllocatorStorage allocatorStorage, RenderLayer layer);

    @Shadow @Final private BlockRenderManager blockRenderManager;

//    @Inject(method = "build", at = @At("HEAD"))
//    public void buildStart(ChunkSectionPos sectionPos, ChunkRendererRegion renderRegion, VertexSorter vertexSorter, BlockBufferAllocatorStorage allocatorStorage, CallbackInfoReturnable<SectionBuilder.RenderData> cir) {
//        DynColorBuilder.setGreenScreening(true);
//    }

//    @Inject(method = "build", at = @At("TAIL"))
//    public void buildEnd(ChunkSectionPos sectionPos, ChunkRendererRegion renderRegion, VertexSorter vertexSorter, BlockBufferAllocatorStorage allocatorStorage, CallbackInfoReturnable<SectionBuilder.RenderData> cir) {
//        DynColorBuilder.setGreenScreening(false);
//    }



    @WrapOperation(method = "build", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/chunk/SectionBuilder;beginBufferBuilding(Ljava/util/Map;Lnet/minecraft/client/render/chunk/BlockBufferAllocatorStorage;Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/BufferBuilder;"))
    public BufferBuilder buildSetBlock(
        SectionBuilder instance,
        Map<RenderLayer, BufferBuilder> builders,
        BlockBufferAllocatorStorage allocatorStorage,
        RenderLayer layer,
        Operation<BufferBuilder> original,
        @Local Map<RenderLayer, BufferBuilder> map,
        @Local RenderLayer renderLayer,
        @Local(name = "blockPos3") BlockPos blockPos
//        @Local(name = "blockState") BlockState blockState
    ) {
        var res = original.call(instance, map, allocatorStorage, renderLayer);
        ((IMixinVertexConsumer)res).rubix$getDeferrer().setBlock(blockPos);
        return res;
    }

//    @Inject(method = "build", at = @At(value = "INVOKE", target = "Ljava/util/Map$Entry;getKey()Ljava/lang/Object;"))
//    public void buildAddDynColor(
//        ChunkSectionPos sectionPos,
//        ChunkRendererRegion renderRegion,
//        VertexSorter vertexSorter,
//        BlockBufferAllocatorStorage allocatorStorage,
//        CallbackInfoReturnable<SectionBuilder.RenderData> cir,
//        @Local Map.Entry<RenderLayer, BufferBuilder> entry,
//        @Local SectionBuilder.RenderData renderData
//    ) {
//        ((IMixinSectionBuilder.RenderData)(Object) renderData).rubix$getDynColorData().put(
//            entry.getKey(),
//            ((IMixinVertexConsumer)entry.getValue()).rubix$getDeferrer().getColorData()
//        );
//    }

    // isn't @Redirect such an amazing thing? you get a strict subset of the functionality of @WrapOperation but with more problems!
    @Inject(method = "build", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getRenderType()Lnet/minecraft/block/BlockRenderType;"))
    private void unHookBuildFabricRenderBlock(
        ChunkSectionPos sectionPos,
        ChunkRendererRegion renderRegion,
        VertexSorter vertexSorter,
        BlockBufferAllocatorStorage allocatorStorage,
        CallbackInfoReturnable<SectionBuilder.RenderData> cir,
        @Local Map<RenderLayer, BufferBuilder> map,
        @Local List<BlockModelPart> list,
        @Local Random random,
        @Local MatrixStack matrixStack,
        @Local BlockState blockState,
        @Local(ordinal = 2) BlockPos blockPos3
    ) {
        if (!EnabledMods.GAY_GRASS_VIDEO) return;
        if (blockState.getRenderType() == BlockRenderType.MODEL) {
            RenderLayer renderLayer = RenderLayers.getBlockLayer(blockState);
            BufferBuilder bufferBuilder = this.beginBufferBuilding(map, allocatorStorage, renderLayer);
            // supposed to be injected
            ((IMixinVertexConsumer)bufferBuilder).rubix$getDeferrer().setBlock(blockPos3);
            random.setSeed(blockState.getRenderingSeed(blockPos3));
            this.blockRenderManager.getModel(blockState).addParts(random, list);
            matrixStack.push();
            matrixStack.translate(
                (float) ChunkSectionPos.getLocalCoord(blockPos3.getX()),
                (float) ChunkSectionPos.getLocalCoord(blockPos3.getY()),
                (float) ChunkSectionPos.getLocalCoord(blockPos3.getZ())
            );
            this.blockRenderManager.renderBlock(blockState, blockPos3, renderRegion, matrixStack, bufferBuilder, true, list);
            matrixStack.pop();
            list.clear();
        }
    }

}
