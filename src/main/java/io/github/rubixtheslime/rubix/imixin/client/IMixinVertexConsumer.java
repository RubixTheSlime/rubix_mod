package io.github.rubixtheslime.rubix.imixin.client;

import io.github.rubixtheslime.rubix.render.DynColorBuilder;

public interface IMixinVertexConsumer {

    default DynColorBuilder rubix$getDeferrer() {
        return null;
    }

}
