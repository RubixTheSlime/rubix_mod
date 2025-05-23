package io.github.rubixtheslime.rubix.mixin;

import io.github.rubixtheslime.rubix.redfile.RedfileTracked;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockEntityTickInvoker.class)
public interface MixinBlockEntityTickInvoker extends RedfileTracked {
    @Shadow BlockPos getPos();

    @Override
    default BlockPos rubix$getPosForRedfile() {
        return getPos().mutableCopy();
    }
}
