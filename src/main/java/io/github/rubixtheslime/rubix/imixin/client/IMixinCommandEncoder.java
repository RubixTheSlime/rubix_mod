package io.github.rubixtheslime.rubix.imixin.client;

import com.mojang.blaze3d.buffers.GpuBuffer;

import java.nio.ByteBuffer;

public interface IMixinCommandEncoder {
    void rubix$unmap(GpuBuffer target);
    ByteBuffer rubix$map(GpuBuffer target, int start, int length);
}
