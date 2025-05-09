package io.github.rubixtheslime.rubix.imixin.client;

import io.github.rubixtheslime.rubix.render.DynColorBuilder;
import io.github.rubixtheslime.rubix.render.DynColorData;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;

public interface IMixinVertexConsumer {

    default DynColorBuilder getDeferrer() {
        return null;
    }

    default void rubix$block(BlockPos pos, BlockState state, BlockColors colors) {}
    default BlockPos rubix$blockPos() {
        return null;
    }
    default BlockState rubix$blockState() {
        return null;
    }
    default void rubix$queueQuad(MatrixStack.Entry entry, BakedQuad quad, int i, int j) {}
    default boolean rubix$shouldDelay() {
        return false;
    }
    default DynColorData rubix$getDynColorData() {
        return null;
    }

}
