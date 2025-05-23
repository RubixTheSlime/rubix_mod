package io.github.rubixtheslime.rubix.mixin;

import io.github.rubixtheslime.rubix.redfile.RedfileTracked;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockPos.class)
public abstract class MixinBlockPos implements RedfileTracked {
    @Shadow public abstract BlockPos.Mutable mutableCopy();

    @Override
    public BlockPos rubix$getPosForRedfile() {
        return mutableCopy();
    }
}
