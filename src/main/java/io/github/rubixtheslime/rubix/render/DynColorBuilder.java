package io.github.rubixtheslime.rubix.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.jtracy.MemoryPool;
import com.mojang.jtracy.TracyClient;
import io.github.rubixtheslime.rubix.client.RubixModClient;
import io.github.rubixtheslime.rubix.util.MoreColor;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.render.*;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class DynColorBuilder implements VertexConsumer {
    private static final MemoryPool MEMORY_POOL = TracyClient.createMemoryPool("DynColorBuffers");
    private static final MemoryUtil.MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator(false);

    private final Map<Long, Map<Integer, List<DynColorVertex>>> deferred = new Long2ObjectOpenHashMap<>();
    private final VertexFormat vertexFormat;

    private long currentBlockPos;
    private int baseColor;
    private DynColorVertex tmpVertex = null;

    public DynColorBuilder(VertexFormat vertexFormat) {
        this.vertexFormat = vertexFormat;
    }

    public void setBlock(BlockPos pos) {
        currentBlockPos = pos.withY(0).asLong();
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        commit();
        tmpVertex = new DynColorVertex();
        tmpVertex.blockPos = currentBlockPos;
        tmpVertex.baseColor = baseColor;
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
        tmpVertex.lightU = (short) u;
        tmpVertex.lightV = (short) v;
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        tmpVertex.normX = x;
        tmpVertex.normY = y;
        tmpVertex.normZ = z;
        return this;
    }

    private void commit() {
        if (tmpVertex == null) return;

        deferred.computeIfAbsent(tmpVertex.blockPos, column -> new Int2ObjectOpenHashMap<>())
            .computeIfAbsent(tmpVertex.baseColor, c -> new ArrayList<>())
            .addLast(tmpVertex);

        tmpVertex = null;
    }

    public boolean isAnimating() {
        return baseColor != 0;
    }

    public void setBaseColor(int color) {
        this.baseColor = color;
    }

    public PartialBuilt finish(VertexConsumer vertexConsumer, int offset) {
        commit();
        List<ColumnEntry> colorData = new ArrayList<>(deferred.size());
        for (var columnEntry : deferred.entrySet()) {
            var pos = BlockPos.fromLong(columnEntry.getKey());
            List<DynColorBuilder.BaseColorEntry> colorEntries = new ArrayList<>(columnEntry.getValue().size());
            for (var colorEntry : columnEntry.getValue().entrySet()) {
                List<Integer> shades = new IntArrayList(colorEntry.getValue().size());
                for (var v : colorEntry.getValue()) {
                    shades.addLast(MoreColor.QuickRgbShadeBlender.precompShade(v.shade));
                    vertexConsumer.vertex(v.vertX, v.vertY, v.vertZ)
                        .color(v.shade, v.shade, v.shade, v.alpha)
                        .texture(v.texU, v.texV)
                        .light(v.lightU, v.lightV)
                        .normal(v.normX, v.normY, v.normZ);
                }
                colorEntries.addLast(new DynColorBuilder.BaseColorEntry(colorEntry.getKey(), shades));
            }
            colorData.addLast(new ColumnEntry(pos.getX(), pos.getZ(), colorEntries));
        }
        return PartialBuilt.of(colorData, offset, vertexFormat);
    }

    public static class PartialBuilt {
        private final List<ColumnEntry> colorData;
        private final int uploadOffset;
        private final int vertexWidth;
        private final int size;

        private PartialBuilt(List<ColumnEntry> colorData, int uploadOffset, int vertexWidth, int size) {
            this.colorData = colorData;
            this.uploadOffset = uploadOffset;
            this.vertexWidth = vertexWidth;
            this.size = size;
        }

        private static PartialBuilt of(List<ColumnEntry> colorData, int skippedVerts, VertexFormat vertexFormat) {
            int vertexWidth = vertexFormat.getVertexSize();
            // color element goes rgba, so start exactly at color and end 4 (exclusive) later
            int uploadOffset = skippedVerts * vertexWidth + vertexFormat.getOffset(VertexFormatElement.COLOR);
            int vertCount = colorData.stream().mapToInt(ColumnEntry::totalVerts).sum();
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
        private final List<ColumnEntry> colorData;
        private final int uploadOffset;
        private final int vertexWidth;
        private final int size;
        private final long bufferPointer;
        private boolean changed = false;
        private boolean closed = false;

        private Built(List<ColumnEntry> colorData, int uploadOffset, int vertexWidth, int size, long bufferPointer) {
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
                int topColor = manager.getColor(new BlockPos(columnEntry.x, 0, columnEntry.z), true);
                if (topColor == columnEntry.lastColor) continue;
                blender.topArgb(topColor);
                changed = true;
                for (var colorEntry : columnEntry.data) {
                    blender.bottomRgb(colorEntry.baseColor);
                    for (int shade : colorEntry.shades) {
                        blender.shadePrecomped(shade);
                        buffer.putInt(injectPoint, blender.getFullAbgr());
                        injectPoint += vertexWidth;
                    }
                }
            }
        }

        public void upload(GpuBuffer gpuBuffer, CommandEncoder commandEncoder) {
            if (changed) commandEncoder.writeToBuffer(gpuBuffer, getBuffer(), uploadOffset);
            changed = false;
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

    private static final class ColumnEntry {
        private final int x;
        private final int z;
        private final List<BaseColorEntry> data;
        private final int lastColor = 0;

        public ColumnEntry(int x, int z, List<BaseColorEntry> data) {
            this.x = x;
            this.z = z;
            this.data = data;
        }

        public int totalVerts() {
            return data.stream().mapToInt(x -> x.shades.size()).sum();
        }

    }

    private record BaseColorEntry(int baseColor, List<Integer> shades) {}

    private static class DynColorVertex {
        private long blockPos;
        private int baseColor;
        private float vertX;
        private float vertY;
        private float vertZ;
        private float shade;
        private short alpha;
        private float texU;
        private float texV;
        private short lightU;
        private short lightV;
        private float normX;
        private float normY;
        private float normZ;

    }

}
