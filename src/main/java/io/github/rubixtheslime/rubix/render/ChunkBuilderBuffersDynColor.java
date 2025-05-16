package io.github.rubixtheslime.rubix.render;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.math.ChunkSectionPos;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class ChunkBuilderBuffersDynColor extends ChunkBuilder.Buffers {
    private ByteBuffer byteBuffer;

    public ChunkBuilderBuffersDynColor(GpuBuffer vertexBuffer, @Nullable GpuBuffer indexBuffer, int indexCount, VertexFormat.IndexType indexType, ByteBuffer byteBuffer) {
        super(vertexBuffer, indexBuffer, indexCount, indexType);
        this.byteBuffer = byteBuffer;
    }

    @Override
    public GpuBuffer getVertexBuffer() {
        var commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        commandEncoder.writeToBuffer(super.getVertexBuffer(), this.byteBuffer, 0);
        return super.getVertexBuffer();
    }

    public void setByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

}
