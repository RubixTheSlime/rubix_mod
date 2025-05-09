package io.github.rubixtheslime.rubix.imixin.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;

public interface IMixinRenderLayer {
    interface MultiPhase {
        int rubix$getAdditionalPasses();

        void rubix$setupAdditionalPass(RenderPass pass, int index);

    }
}
