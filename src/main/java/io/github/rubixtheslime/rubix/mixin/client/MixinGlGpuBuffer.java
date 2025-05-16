package io.github.rubixtheslime.rubix.mixin.client;

import io.github.rubixtheslime.rubix.imixin.client.IMixinGlGpuBuffer;
import net.minecraft.client.gl.GlGpuBuffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GlGpuBuffer.class)
public class MixinGlGpuBuffer implements IMixinGlGpuBuffer {
    @Shadow @Final protected int id;

    @Override
    public int rubix$getId() {
        return id;
    }
}
