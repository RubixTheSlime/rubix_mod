package io.github.rubixtheslime.rubix.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.rubixtheslime.rubix.imixin.client.IMixinChunkBuilder;
import io.github.rubixtheslime.rubix.imixin.client.IMixinSectionBuilder;
import io.github.rubixtheslime.rubix.render.DynColorBuilder;
import io.github.rubixtheslime.rubix.render.DynColorData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.SectionBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Mixin(ChunkBuilder.class)
public class MixinChunkBuilder implements IMixinChunkBuilder {

    @Mixin(ChunkBuilder.BuiltChunk.class)
    public static class BuiltChunk {

        @Mixin(targets = "net.minecraft.client.render.chunk.ChunkBuilder$BuiltChunk$RebuildTask")
        public static class RebuildTask {

            @Inject(method = "run", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/chunk/SectionBuilder$RenderData;chunkOcclusionData:Lnet/minecraft/client/render/chunk/ChunkOcclusionData;"))
            public void run(
                BlockBufferAllocatorStorage buffers,
                CallbackInfoReturnable<CompletableFuture<ChunkBuilder.Result>> cir,
                @Local SectionBuilder.RenderData renderData,
                @Local ChunkBuilder.ChunkData chunkData
            ) {
                ((IMixinChunkBuilder.ChunkData)chunkData).rubix$setDynColorData(((IMixinSectionBuilder.RenderData)(Object) renderData).rubix$getDynColorData());
            }

        }

    }

    @Mixin(ChunkBuilder.ChunkData.class)
    public static class ChunkData implements IMixinChunkBuilder.ChunkData {
        @Unique
        private Map<RenderLayer, DynColorBuilder.BuiltData> map;

        @Override
        public void rubix$setDynColorData(Map<RenderLayer, DynColorBuilder.BuiltData> map) {
            this.map = map;
        }

        @Override
        public Map<RenderLayer, DynColorBuilder.BuiltData> rubix$getDynColorData() {
            return map;
        }
    }

}
