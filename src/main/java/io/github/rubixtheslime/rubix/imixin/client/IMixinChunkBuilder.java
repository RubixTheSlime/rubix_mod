package io.github.rubixtheslime.rubix.imixin.client;

import io.github.rubixtheslime.rubix.render.DynColorBuilder;
import io.github.rubixtheslime.rubix.render.DynColorData;
import net.minecraft.client.render.RenderLayer;

import java.util.Map;

public interface IMixinChunkBuilder {
    interface ChunkData {
        void rubix$setDynColorData(Map<RenderLayer, DynColorBuilder.BuiltData> map);
        Map<RenderLayer, DynColorBuilder.BuiltData> rubix$getDynColorData();
    }
}
