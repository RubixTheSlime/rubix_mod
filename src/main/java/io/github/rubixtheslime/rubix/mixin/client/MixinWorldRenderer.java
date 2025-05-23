package io.github.rubixtheslime.rubix.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.rubixtheslime.rubix.EnabledMods;
import io.github.rubixtheslime.rubix.client.RubixModClient;
import io.github.rubixtheslime.rubix.gaygrass.PrideFlagManager;
import io.github.rubixtheslime.rubix.imixin.client.IMixinChunkBuilder;
import io.github.rubixtheslime.rubix.imixin.client.IMixinMinecraftClient;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {
    @Shadow private @Nullable ClientWorld world;

    @Shadow @Final private MinecraftClient client;

    @Shadow private Frustum frustum;

    @Shadow private @Nullable ChunkBuilder chunkBuilder;

    @Shadow @Final private ObjectArrayList<ChunkBuilder.BuiltChunk> builtChunks;

    @Shadow protected abstract void renderLayer(RenderLayer renderLayer, double x, double y, double z, Matrix4f viewMatrix, Matrix4f positionMatrix);

    @Inject(method = "render", at = @At("TAIL"))
    public void render(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (EnabledMods.REDFILE) {
            MatrixStack stack = new MatrixStack();
            stack.multiplyPositionMatrix(positionMatrix);
            ((IMixinMinecraftClient) client).rubix$getRedfileResultManager().render(stack, client.gameRenderer.getCamera().getPos(), frustum, world);
        }
        if (EnabledMods.GAY_GRASS_VIDEO) {
            RubixModClient.prideFlagManager.setTime(System.currentTimeMillis() / 16);
        }
    }

    @Inject(method = "renderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;startDrawing()V"))
    void renderLayerUploadDynColor(
        RenderLayer renderLayer,
        double x,
        double y,
        double z,
        Matrix4f viewMatrix,
        Matrix4f positionMatrix,
        CallbackInfo ci
    ) {
        if (!EnabledMods.GAY_GRASS_VIDEO) return;
        for (var builtChunk : builtChunks) {
            var buffers = builtChunk.getBuffers(renderLayer);
            if (buffers == null) continue;
            ((IMixinChunkBuilder.Buffers)buffers).rubix$updateDynColorDataFuture();
        }
//        var list = builtChunks.stream()
//            .map(builtChunk -> builtChunk.getBuffers(renderLayer))
//            .filter(Objects::nonNull)
//            .map(buffers -> ((IMixinChunkBuilder.Buffers)buffers).rubix$updateDynColorDataFuture())
//            .toList();
//        try {
//            Util.combine(list).get();
//        } catch (InterruptedException | ExecutionException e) {
//            throw new RuntimeException(e);
//        }
    }


    @Inject(method = "renderLayer", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/render/chunk/ChunkBuilder$Buffers;getIndexBuffer()Lcom/mojang/blaze3d/buffers/GpuBuffer;"))
    public void renderLayerDynColor(
        RenderLayer renderLayer,
        double x,
        double y,
        double z,
        Matrix4f viewMatrix,
        Matrix4f positionMatrix,
        CallbackInfo ci,
        @Local ChunkBuilder.Buffers buffers
    ) {
        if (!EnabledMods.GAY_GRASS_VIDEO) return;
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        ((IMixinChunkBuilder.Buffers)buffers).rubix$uploadDynColorData(commandEncoder, chunkBuilder == null || ((IMixinChunkBuilder)chunkBuilder).rubix$isStopped());
    }

}
