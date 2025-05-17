package io.github.rubixtheslime.rubix.imixin.client;

import com.mojang.blaze3d.systems.CommandEncoder;
import io.github.rubixtheslime.rubix.render.DynColorBuilder;

import java.util.concurrent.CompletableFuture;

public interface IMixinChunkBuilder {
    boolean rubix$isStopped();

    interface Buffers {
        void rubix$setDynColorData(DynColorBuilder.Built data);
        CompletableFuture<Void> rubix$updateDynColorDataFuture();

        void rubix$uploadDynColorData(CommandEncoder commandEncoder, boolean stopped);
    }
}
