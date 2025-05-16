package io.github.rubixtheslime.rubix.mixin.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import io.github.rubixtheslime.rubix.imixin.client.IMixinCommandEncoder;
import io.github.rubixtheslime.rubix.imixin.client.IMixinGlGpuBuffer;
import net.minecraft.client.gl.GlGpuBuffer;
import net.minecraft.client.gl.GlResourceManager;
import org.spongepowered.asm.mixin.Mixin;

import java.nio.ByteBuffer;

@Mixin(GlResourceManager.class)
public class MixinGlResourceManager implements IMixinCommandEncoder {

    @Override
    public void rubix$unmap(GpuBuffer target) {
        GlGpuBuffer glGpuBuffer = (GlGpuBuffer) target;
        GlStateManager._glBindBuffer(GlConst.toGl(glGpuBuffer.type()), ((IMixinGlGpuBuffer) glGpuBuffer).rubix$getId());
        GlStateManager._glUnmapBuffer(GlConst.toGl(glGpuBuffer.type()));
    }

    @Override
    public ByteBuffer rubix$map(GpuBuffer target, int start, int length) {
        GlGpuBuffer glGpuBuffer = (GlGpuBuffer) target;
        if (glGpuBuffer.isClosed()) {
            throw new IllegalStateException("Buffer already closed");
        } else {
            GlStateManager._glBindBuffer(GlConst.toGl(glGpuBuffer.type()), ((IMixinGlGpuBuffer) glGpuBuffer).rubix$getId());
            return GlStateManager._glMapBufferRange(GlConst.toGl(glGpuBuffer.type()), start, length, GlConst.toGl(glGpuBuffer.usage()));
        }
    }
}
