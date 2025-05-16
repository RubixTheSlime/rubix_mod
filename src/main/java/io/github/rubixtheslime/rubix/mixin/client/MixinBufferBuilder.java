package io.github.rubixtheslime.rubix.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.rubixtheslime.rubix.RDebug;
import io.github.rubixtheslime.rubix.client.RubixModClient;
import io.github.rubixtheslime.rubix.imixin.client.IMixinBufferBuilder;
import io.github.rubixtheslime.rubix.imixin.client.IMixinBuiltBuffer;
import io.github.rubixtheslime.rubix.imixin.client.IMixinVertexConsumer;
import io.github.rubixtheslime.rubix.render.DynColorBuilder;
import io.github.rubixtheslime.rubix.util.MoreColor;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = BufferBuilder.class, priority = 2000)
public abstract class MixinBufferBuilder implements VertexConsumer, IMixinBufferBuilder, IMixinVertexConsumer {
    @Shadow @Final private BufferAllocator allocator;
//    @Unique
//    private DynColorData layerEntry;
//    @Unique
//    private BlockPos blockPos;
//    @Unique
//    private BlockState blockState;
//    @Unique
//    private Map<MatrixStack.Entry, MatrixStack.Entry> matrixEntrySet;

    @Shadow @Final private VertexFormat.DrawMode drawMode;
    @Shadow private int vertexCount;
    @Shadow @Final private VertexFormat vertexFormat;
    @Unique private DynColorBuilder deferrer = null;

//    @Inject(method = "vertex(FFF)Lnet/minecraft/client/render/VertexConsumer;", at = @At("HEAD"), cancellable = true)
//    public void deferVertex(float x, float y, float z, CallbackInfoReturnable<VertexConsumer> cir) {
//        if (DynColorBuilder.isGreenScreening()) cir.setReturnValue(deferrer.vertex(x, y, z));
//    }
//
//    @Inject(method = "color(IIII)Lnet/minecraft/client/render/VertexConsumer;", at = @At("HEAD"), cancellable = true)
//    public void deferColor(int red, int green, int blue, int alpha, CallbackInfoReturnable<VertexConsumer> cir) {
//        if (DynColorBuilder.isGreenScreening()) cir.setReturnValue(deferrer.color(red, green, blue, alpha));
//    }
//
//    @Inject(method = "texture", at = @At("HEAD"), cancellable = true)
//    public void deferTexture(float u, float v, CallbackInfoReturnable<VertexConsumer> cir) {
//        if (DynColorBuilder.isGreenScreening()) cir.setReturnValue(deferrer.texture(u, v));
//    }
//
//    @Inject(method = "light(II)Lnet/minecraft/client/render/VertexConsumer;", at = @At("HEAD"), cancellable = true)
//    public void deferLight(int u, int v, CallbackInfoReturnable<VertexConsumer> cir) {
//        if (DynColorBuilder.isGreenScreening()) cir.setReturnValue(deferrer.light(u, v));
//    }
//
//    @Inject(method = "normal", at = @At("HEAD"), cancellable = true)
//    public void deferNormal(float x, float y, float z, CallbackInfoReturnable<VertexConsumer> cir) {
//        if (DynColorBuilder.isGreenScreening()) cir.setReturnValue(deferrer.normal(x, y, z));
//    }
//
//    @Inject(method = "endNullable", at = @At("HEAD"))
//    public void end(CallbackInfoReturnable<BuiltBuffer> cir) {
//        deferrer.endDirect();
//    }
//
//    @Override
//    public DynColorBuilder.BuiltData rubix$getBuiltDynColorData() {
//        return deferrer.getColorData();
//    }

    @Override
    public DynColorBuilder rubix$getDeferrer() {
        if (deferrer == null) deferrer = new DynColorBuilder(this.drawMode);
        return deferrer;
    }

    @WrapMethod(method = "endNullable")
    BuiltBuffer build(Operation<BuiltBuffer> original) {
        if (deferrer == null) return original.call();
        int skipped = vertexCount;
        var map = deferrer.getDeferred();
        List<DynColorBuilder.ColEntry> colorData = new ArrayList<>(map.size());
        for (var columnEntry : map.entrySet()) {
            var pos = BlockPos.fromLong(columnEntry.getKey());
            List<DynColorBuilder.BaseColorEntry> colorEntries = new ArrayList<>(columnEntry.getValue().size());
            for (var colorEntry : columnEntry.getValue().entrySet()) {
                List<Integer> shades = new IntArrayList(colorEntry.getValue().size());
                for (var v : colorEntry.getValue()) {
                    shades.addLast(MoreColor.QuickRgbShadeBlender.precompShade(v.shade()));
//                    shades.addLast((int) v.shade());
                    this.vertex(v.vertX(), v.vertY(), v.vertZ())
                        .color(v.shade(), v.shade(), v.shade(), v.alpha())
                        .texture(v.texU(), v.texV())
                        .light(v.lightU(), v.lightV())
                        .normal(v.normX(), v.normY(), v.normZ());
                }
                colorEntries.addLast(new DynColorBuilder.BaseColorEntry(colorEntry.getKey(), shades));
            }
            colorData.addLast(new DynColorBuilder.ColEntry(pos.getX(), pos.getZ(), colorEntries));
        }

        var res = original.call();
        if (res == null) return null;

        ((IMixinBuiltBuffer)res).rubix$setDynColor(DynColorBuilder.PartialBuilt.of(colorData, skipped, vertexFormat));
        return res;
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