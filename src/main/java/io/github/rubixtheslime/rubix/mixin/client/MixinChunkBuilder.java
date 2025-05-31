package io.github.rubixtheslime.rubix.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import io.github.rubixtheslime.rubix.EnabledMods;
import io.github.rubixtheslime.rubix.imixin.client.IMixinBuiltBuffer;
import io.github.rubixtheslime.rubix.imixin.client.IMixinChunkBuilder;
import io.github.rubixtheslime.rubix.render.DynColorBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkBuilder.class)
public class MixinChunkBuilder implements IMixinChunkBuilder {

    @Shadow
    volatile boolean stopped;

    @Override
    public boolean rubix$isStopped() {
        return stopped;
    }

    @Mixin(ChunkBuilder.BuiltChunk.class)
    public static class BuiltChunk {

        @Unique
        void setDynColorData(ChunkBuilder.Buffers buffers, BuiltBuffer builtBuffer) {
            if (!EnabledMods.GAY_GRASS_VIDEO) return;
            var ibuffers = ((IMixinChunkBuilder.Buffers) buffers);
            ibuffers.rubix$setDynColorData(((IMixinBuiltBuffer) builtBuffer).rubix$getDynColor());
            ibuffers.rubix$updateDynColorData();
        }

        @ModifyExpressionValue(method = "method_68535", at = @At(value = "FIELD", remap = false, target = "Lcom/mojang/blaze3d/buffers/BufferUsage;STATIC_WRITE:Lcom/mojang/blaze3d/buffers/BufferUsage;"))
        BufferUsage makeDynamic(BufferUsage original, RenderLayer layer, BuiltBuffer builtBuffer) {
            return EnabledMods.GAY_GRASS_VIDEO && ((IMixinBuiltBuffer)builtBuffer).rubix$hasDynColor() ? BufferUsage.DYNAMIC_WRITE : original;
        }

        @Inject(method = "method_68535", at = @At(value = "INVOKE", ordinal = 0, target = "Lcom/mojang/blaze3d/systems/CommandEncoder;writeToBuffer(Lcom/mojang/blaze3d/buffers/GpuBuffer;Ljava/nio/ByteBuffer;I)V"))
        void setByteBufferUpdate(
            RenderLayer renderLayer,
            BuiltBuffer builtBuffer,
            CallbackInfo ci,
            @Local ChunkBuilder.Buffers buffers
        ){
            setDynColorData(buffers, builtBuffer);
        }

        @Inject(method = "method_68535", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/chunk/ChunkBuilder$Buffers;setVertexBuffer(Lcom/mojang/blaze3d/buffers/GpuBuffer;)V"))
        void setByteBufferExpand(
            RenderLayer renderLayer,
            BuiltBuffer builtBuffer,
            CallbackInfo ci,
            @Local ChunkBuilder.Buffers buffers
        ){
            setDynColorData(buffers, builtBuffer);
        }

        @Inject(method = "method_68535", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
        void setByteBufferNew(
            RenderLayer renderLayer,
            BuiltBuffer builtBuffer,
            CallbackInfo ci,
            @Local ChunkBuilder.Buffers buffers
        ){
            setDynColorData(buffers, builtBuffer);
        }

    }

    @Mixin(ChunkBuilder.Buffers.class)
    public static class Buffers implements IMixinChunkBuilder.Buffers {
        @Shadow
        GpuBuffer vertexBuffer;
        @Unique DynColorBuilder.Built colorData;

        @Override
        public void rubix$setDynColorData(DynColorBuilder.Built data) {
            if (colorData != null) colorData.close();
            colorData = data;
        }

        @Override
        public void rubix$updateDynColorData() {
            if (colorData != null) colorData.update();
        }

        @Override
        public CompletableFuture<Void> rubix$updateDynColorDataFuture() {
            if (colorData == null) return CompletableFuture.completedFuture(null);
            return CompletableFuture.runAsync(() -> colorData.update());
        }

        @Override
        public void rubix$uploadDynColorData(CommandEncoder commandEncoder, boolean stopped) {
            if (colorData == null) return;
            if (stopped) {
                colorData.close();
                return;
            }
            colorData.upload(vertexBuffer, commandEncoder);
        }

    }

}
