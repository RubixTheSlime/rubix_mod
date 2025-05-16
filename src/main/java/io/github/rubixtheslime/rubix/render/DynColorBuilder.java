package io.github.rubixtheslime.rubix.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.jtracy.MemoryPool;
import com.mojang.jtracy.TracyClient;
import io.github.rubixtheslime.rubix.RDebug;
import io.github.rubixtheslime.rubix.client.RubixModClient;
import io.github.rubixtheslime.rubix.util.MoreColor;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.render.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class DynColorBuilder implements VertexConsumer {
    private static final MemoryPool MEMORY_POOL = TracyClient.createMemoryPool("DynColorBuffers");
    private static final MemoryUtil.MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator(false);

    private final VertexFormat.DrawMode drawMode;
    private final List<BuildingVertex> quadBuffer = new ArrayList<>(4);
    private final Map<Long, Map<Integer, List<BuiltColorVertex>>> deferred = new Long2ObjectOpenHashMap<>();

    private long currentBlockPos;
    private int baseColor;
    private BlockPos prevPos = null;
    private int prevColor;
    private BuildingVertex tmpVertex = null;

    public DynColorBuilder(VertexFormat.DrawMode drawMode) {
        this.drawMode = drawMode;
    }

    public void setBlock(BlockPos pos) {
//        commit(false);
        currentBlockPos = pos.withY(0).asLong();
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        commit();
        tmpVertex = new BuildingVertex();
        // ??????????? i have no idea why this is necessary, somehow everything gets misaligned
//        if (prevPos == null) {
            tmpVertex.blockPos = currentBlockPos;
            tmpVertex.baseColor = baseColor;
//        } else {
//            tmpVertex.blockPos = prevPos;
//            tmpVertex.baseColor = prevColor;
//        }
//        prevPos = currentBlockPos;
//        prevColor = baseColor;
        tmpVertex.vertX = x;
        tmpVertex.vertY = y;
        tmpVertex.vertZ = z;
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        int baseColorShade = MoreColor.maxComponentRgb(tmpVertex.baseColor);
        int component = Math.max(Math.max(red, green), blue);
        tmpVertex.shade = Math.clamp((float) component / (float) baseColorShade, 0, 1);
        tmpVertex.alpha = (short) alpha;
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        tmpVertex.texU = u;
        tmpVertex.texV = v;
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        tmpVertex.lightU = u;
        tmpVertex.lightV = v;
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        tmpVertex.normX = x;
        tmpVertex.normY = y;
        tmpVertex.normZ = z;
        return this;
    }

    public Map<Long, Map<Integer, List<BuiltColorVertex>>> getDeferred() {
        commit();
        int total = deferred.values().stream().mapToInt(a ->
            a.values().stream().mapToInt(List::size).sum()
        ).sum();
        assert total % 4 == 0;
        return deferred;
    }

    private void commit() {
        if (tmpVertex == null) {
            return;
        }

        var list = deferred
            .computeIfAbsent(tmpVertex.blockPos, column -> new Int2ObjectOpenHashMap<>())
            .computeIfAbsent(tmpVertex.baseColor, c -> new ArrayList<>());

        list.addLast(tmpVertex.finish());

//        quadBuffer.add(tmpVertex);
//        if (quadBuffer.size() < 4) return;
//        var main = quadBuffer.get(RDebug.i0());
//        var list = deferred
//            .computeIfAbsent(main.blockPos.withY(0).asLong(), column -> new Int2ObjectOpenHashMap<>())
//            .computeIfAbsent(main.baseColor, c -> new ArrayList<>());
//        for (var v : quadBuffer) {
//            list.addLast(v.finish());
//        }
//        quadBuffer.clear();

        tmpVertex = null;
    }

    public boolean isAnimating() {
        return baseColor != 0;
    }

    public void setBaseColor(int color) {
//        commit(false);
        this.baseColor = color;
    }

    public static class PartialBuilt {
        private final List<ColEntry> colorData;
        private final int uploadOffset;
        private final int vertexWidth;
        private final int size;

        private PartialBuilt(List<ColEntry> colorData, int uploadOffset, int vertexWidth, int size) {
            this.colorData = colorData;
            this.uploadOffset = uploadOffset;
            this.vertexWidth = vertexWidth;
            this.size = size;
        }

        public static PartialBuilt of(List<ColEntry> colorData, int skippedVerts, VertexFormat vertexFormat) {
            int vertexWidth = vertexFormat.getVertexSize();
            // color element goes rgba, so start exactly at color and end 4 (exclusive) later
            int uploadOffset = skippedVerts * vertexWidth + vertexFormat.getOffset(VertexFormatElement.COLOR);
            int vertCount = colorData.stream().mapToInt(ColEntry::totalVerts).sum();
            if (vertCount == 0) return null;
            int size = vertexWidth * (vertCount - 1) + 4;
            return new PartialBuilt(colorData, uploadOffset, vertexWidth, size);
        }

        public Built build(ByteBuffer fullBuffer) {
            var bufferPointer = ALLOCATOR.malloc(size);
            MEMORY_POOL.malloc(bufferPointer, size);
            MemoryUtil.memCopy(fullBuffer.slice(uploadOffset, size), MemoryUtil.memByteBuffer(bufferPointer, size));
            return new Built(colorData, uploadOffset, vertexWidth, size, bufferPointer);
        }

    }

    public static class Built {
        private final List<ColEntry> colorData;
        private final int uploadOffset;
        private final int vertexWidth;
        private final int size;
        private final long bufferPointer;
        private boolean closed = false;

        private Built(List<ColEntry> colorData, int uploadOffset, int vertexWidth, int size, long bufferPointer) {
            this.colorData = colorData;
            this.uploadOffset = uploadOffset;
            this.vertexWidth = vertexWidth;
            this.size = size;
            this.bufferPointer = bufferPointer;
        }

        public void update() {
            var manager = RubixModClient.prideFlagManager;
            var blender = new MoreColor.QuickRgbShadeBlender();
            var buffer = getBuffer();
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            int injectPoint = 0;
            for (var columnEntry : colorData) {
                blender.topArgb(manager.getColor(new BlockPos(columnEntry.x, 0, columnEntry.z), true));
                for (var colorEntry : columnEntry.data) {
                    blender.bottomRgb(colorEntry.baseColor);
                    for (int shade : colorEntry.shades) {
                        blender.shadePrecomped(shade);
                        int color = blender.getFullAbgr();
                        buffer.putInt(injectPoint, color);
                        injectPoint += vertexWidth;
                    }
                }
            }
        }

        public void upload(GpuBuffer gpuBuffer, CommandEncoder commandEncoder) {
            commandEncoder.writeToBuffer(gpuBuffer, getBuffer(), uploadOffset);
        }

        public ByteBuffer getBuffer() {
            if (closed) {
                throw new IllegalStateException("buffer already closed");
            } else {
                return MemoryUtil.memByteBuffer(this.bufferPointer, this.size);
            }
        }

        public void close() {
            if (closed) return;
            closed = true;
            MEMORY_POOL.free(bufferPointer);
            ALLOCATOR.free(bufferPointer);
        }
    }

    public record ColEntry(int x, int z, List<BaseColorEntry> data) {
        public int totalVerts() {
            return data.stream().mapToInt(x -> x.shades.size()).sum();
        }
    }

    public record BaseColorEntry(int baseColor, List<Integer> shades) {}

    public record BuiltColorVertex(float vertX, float vertY, float vertZ, float shade, short alpha, float texU,
                                    float texV, short lightU, short lightV, float normX, float normY, float normZ) {
    }

    private static class BuildingVertex {
        private long blockPos;
        private int baseColor;
        private float vertX;
        private float vertY;
        private float vertZ;
        private float shade;
        private short alpha;
        private float texU;
        private float texV;
        private int lightU;
        private int lightV;
        private float normX;
        private float normY;
        private float normZ;

        private BuiltColorVertex finish() {
            return new BuiltColorVertex(
                vertX, vertY, vertZ,
                shade, alpha,
                texU, texV,
                (short) lightU, (short) lightV,
                normX, normY, normZ
            );
        }

        private VertexConsumer applyMost(VertexConsumer vertexConsumer) {
            return vertexConsumer.vertex(vertX, vertY, vertZ)
                .texture(texU, texV)
                .light(lightU, lightV)
                .normal(normX, normY, normZ);
        }
    }

}
