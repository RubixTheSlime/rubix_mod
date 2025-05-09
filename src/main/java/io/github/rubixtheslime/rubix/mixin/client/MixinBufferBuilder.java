package io.github.rubixtheslime.rubix.mixin.client;

import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.rubixtheslime.rubix.RDebug;
import io.github.rubixtheslime.rubix.client.RubixModClient;
import io.github.rubixtheslime.rubix.imixin.client.IMixinBufferBuilder;
import io.github.rubixtheslime.rubix.imixin.client.IMixinVertexConsumer;
import io.github.rubixtheslime.rubix.render.DynColorBuilder;
import io.github.rubixtheslime.rubix.render.DynColorData;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Mixin(value = BufferBuilder.class, priority = 2000)
public abstract class MixinBufferBuilder implements VertexConsumer, IMixinBufferBuilder {
    @Shadow @Final private BufferAllocator allocator;
    @Unique
    private DynColorData layerEntry;
    @Unique
    private BlockPos blockPos;
    @Unique
    private BlockState blockState;
    @Unique
    private Map<MatrixStack.Entry, MatrixStack.Entry> matrixEntrySet;

    @Unique private final DynColorBuilder deferrer = new DynColorBuilder(this);

    @Inject(method = "vertex(FFF)Lnet/minecraft/client/render/VertexConsumer;", at = @At("HEAD"), cancellable = true)
    public void deferVertex(float x, float y, float z, CallbackInfoReturnable<VertexConsumer> cir) {
        if (DynColorBuilder.isGreenScreening()) cir.setReturnValue(deferrer.vertex(x, y, z));
    }

    @Inject(method = "color(IIII)Lnet/minecraft/client/render/VertexConsumer;", at = @At("HEAD"), cancellable = true)
    public void deferColor(int red, int green, int blue, int alpha, CallbackInfoReturnable<VertexConsumer> cir) {
        if (DynColorBuilder.isGreenScreening()) cir.setReturnValue(deferrer.color(red, green, blue, alpha));
    }

    @Inject(method = "texture", at = @At("HEAD"), cancellable = true)
    public void deferTexture(float u, float v, CallbackInfoReturnable<VertexConsumer> cir) {
        if (DynColorBuilder.isGreenScreening()) cir.setReturnValue(deferrer.texture(u, v));
    }

    @Inject(method = "light(II)Lnet/minecraft/client/render/VertexConsumer;", at = @At("HEAD"), cancellable = true)
    public void deferLight(int u, int v, CallbackInfoReturnable<VertexConsumer> cir) {
        if (DynColorBuilder.isGreenScreening()) cir.setReturnValue(deferrer.light(u, v));
    }

    @Inject(method = "normal", at = @At("HEAD"), cancellable = true)
    public void deferNormal(float x, float y, float z, CallbackInfoReturnable<VertexConsumer> cir) {
        if (DynColorBuilder.isGreenScreening()) cir.setReturnValue(deferrer.normal(x, y, z));
    }

    @Inject(method = "endNullable", at = @At("HEAD"))
    public void end(CallbackInfoReturnable<BuiltBuffer> cir) {
        deferrer.endDirect();
    }

    @Override
    public DynColorBuilder.BuiltData rubix$getBuiltDynColorData() {
        return deferrer.getColorData();
    }

    //    @Override
//    public void rubix$block(BlockPos pos, BlockState state, BlockColors colors) {
//        if (RDebug.b2() && colors.getProperties(state.getBlock()).isEmpty()) return;
//        if (RDebug.b3() && !RubixModClient.prideFlagManager.isAnimated(pos)) return;
//        if (this.layerEntry == null) this.layerEntry = new DynColorData(new HashMap<>());
//        blockPos = pos;
//        blockState = state;
//    }

//    @Override
//    public void rubix$queueQuad(MatrixStack.Entry entry, BakedQuad quad, int i, int j) {
//        if (layerEntry == null) return;
//        if (matrixEntrySet == null) matrixEntrySet = new Object2ReferenceOpenHashMap<>();
//        MatrixStack.Entry copied = matrixEntrySet.computeIfAbsent(entry, MatrixStack.Entry::copy);
//        var quadEntry = new DynColorData.QuadEntry(copied, quad, i, j);
//        layerEntry.blocks()
//            .computeIfAbsent(blockPos, x -> new DynColorData.BlockEntry(blockState, new ArrayList<>()))
//            .quads()
//            .add(quadEntry);
//    }

//    @Override
//    public DynColorData rubix$getDynColorData() {
//        return layerEntry == null || layerEntry.blocks().isEmpty() ? null : layerEntry;
//    }

//    @Override
//    public boolean rubix$shouldDelay() {
//        return layerEntry != null && blockPos != null && blockState != null;
//    }

}