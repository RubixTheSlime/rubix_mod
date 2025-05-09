package io.github.rubixtheslime.rubix.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.VertexSorter;
import io.github.rubixtheslime.rubix.imixin.client.IMixinBufferBuilder;
import io.github.rubixtheslime.rubix.imixin.client.IMixinSectionBuilder;
import io.github.rubixtheslime.rubix.imixin.client.IMixinVertexConsumer;
import io.github.rubixtheslime.rubix.render.DynColorBuilder;
import io.github.rubixtheslime.rubix.render.DynColorData;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = SectionBuilder.class, priority = 500)
public class MixinSectionBuilder implements IMixinSectionBuilder {

    @Inject(method = "build", at = @At("HEAD"))
    public void buildStart(ChunkSectionPos sectionPos, ChunkRendererRegion renderRegion, VertexSorter vertexSorter, BlockBufferAllocatorStorage allocatorStorage, CallbackInfoReturnable<SectionBuilder.RenderData> cir) {
        DynColorBuilder.setGreenScreening(true);
    }

    @Inject(method = "build", at = @At("TAIL"))
    public void buildEnd(ChunkSectionPos sectionPos, ChunkRendererRegion renderRegion, VertexSorter vertexSorter, BlockBufferAllocatorStorage allocatorStorage, CallbackInfoReturnable<SectionBuilder.RenderData> cir) {
        DynColorBuilder.setGreenScreening(false);
    }

    @Inject(method = "build", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOpaqueFullCube()Z"))
    public void buildSetBlock(
        ChunkSectionPos sectionPos,
        ChunkRendererRegion renderRegion,
        VertexSorter vertexSorter,
        BlockBufferAllocatorStorage allocatorStorage,
        CallbackInfoReturnable<SectionBuilder.RenderData> cir,
        @Local(name = "blockPos3") BlockPos blockPos,
        @Local(name = "blockState") BlockState blockState
    ) {
        DynColorBuilder.setBlock(blockPos, blockState);
    }

    @Inject(method = "build", at = @At(value = "INVOKE", target = "Ljava/util/Map$Entry;getKey()Ljava/lang/Object;"))
    public void buildAddDynColor(
        ChunkSectionPos sectionPos,
        ChunkRendererRegion renderRegion,
        VertexSorter vertexSorter,
        BlockBufferAllocatorStorage allocatorStorage,
        CallbackInfoReturnable<SectionBuilder.RenderData> cir,
        @Local Map.Entry<RenderLayer, BufferBuilder> entry,
        @Local SectionBuilder.RenderData renderData
    ) {
        ((IMixinSectionBuilder.RenderData)(Object) renderData).rubix$getDynColorData().put(
            entry.getKey(),
            ((IMixinBufferBuilder)entry.getValue()).rubix$getBuiltDynColorData()
        );
    }

    @Mixin(SectionBuilder.RenderData.class)
    public static class RenderData implements IMixinSectionBuilder.RenderData {
        @Unique
        private Map<RenderLayer, DynColorBuilder.BuiltData> dynColorData;

        @Override
        public Map<RenderLayer, DynColorBuilder.BuiltData> rubix$getDynColorData() {
            if (dynColorData == null) dynColorData = new Object2ObjectOpenHashMap<>();
            return dynColorData;
        }
    }


    @WrapOperation(method = "build", at = @At(value = "INVOKE", target = "net/minecraft/block/BlockState.getRenderType()Lnet/minecraft/block/BlockRenderType;"))
    private BlockRenderType unHookBuildRenderBlock(BlockState instance, Operation<BlockRenderType> original, @Local(ordinal = 2) BlockPos blockPos) {
        return instance.getRenderType();
    }

}
