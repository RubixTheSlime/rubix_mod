package io.github.rubixtheslime.rubix.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.rubixtheslime.rubix.EnabledMods;
import io.github.rubixtheslime.rubix.imixin.client.IMixinChunkBuilder;
import io.github.rubixtheslime.rubix.imixin.client.IMixinMinecraftClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {
    @Shadow private @Nullable ClientWorld world;

    @Shadow @Final private MinecraftClient client;

    @Shadow private Frustum frustum;

    @Shadow @Final private BufferBuilderStorage bufferBuilders;

    @Inject(method = "render", at = @At("TAIL"))
    public void render(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (!EnabledMods.REDFILE) return;
        MatrixStack stack = new MatrixStack();
        stack.multiplyPositionMatrix(positionMatrix);
//        stack.translate(client.gameRenderer.getCamera().getPos());
        ((IMixinMinecraftClient) client).rubix$getRedfileResultManager().render(stack, client.gameRenderer.getCamera().getPos(), frustum, world);
    }

    @Inject(method = "renderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk;getBuffers(Lnet/minecraft/client/render/RenderLayer;)Lnet/minecraft/client/render/chunk/ChunkBuilder$Buffers;"))
    public void renderLayerDynColor(RenderLayer renderLayer, double x, double y, double z, Matrix4f viewMatrix, Matrix4f positionMatrix, CallbackInfo ci, @Local ChunkBuilder.BuiltChunk builtChunk) {
        var builtChunkData = builtChunk.getData();
        if (builtChunkData == null) return;
        var dataMap = ((IMixinChunkBuilder.ChunkData)builtChunkData).rubix$getDynColorData();
        if (dataMap == null) return;
        var data = dataMap.get(renderLayer);
        if (data == null) return;
        var bufferBuilder = new BufferBuilder(bufferBuilders.getBlockBufferBuilders().get(renderLayer), VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
        var origin = builtChunk.getOrigin();
        data.draw(bufferBuilder,
            (float) ((double) origin.getX() - x),
            (float) ((double) origin.getY() - y),
            (float) ((double) origin.getZ() - z));
        var built = bufferBuilder.endNullable();
        if (built == null) return;
        renderLayer.draw(built);
    }

}
