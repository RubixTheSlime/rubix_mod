package io.github.rubixtheslime.rubix.imixin.client;

import io.github.rubixtheslime.rubix.render.DynColorBuilder;

public interface IMixinBuiltBuffer {
    void rubix$setDynColor(DynColorBuilder.PartialBuilt built);
    DynColorBuilder.Built rubix$getDynColor();
    boolean rubix$hasDynColor();
}
