package io.github.rubixtheslime.rubix.mixin;

import io.github.rubixtheslime.rubix.redfile.RedfileTag;
import io.github.rubixtheslime.rubix.redfile.RedfileTags;
import io.github.rubixtheslime.rubix.redfile.RedfileTracker;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockPos.class)
public abstract class MixinBlockPos implements RedfileTracker {

    @Shadow public abstract BlockPos toImmutable();

    @Override
    public BlockPos getPosForRedfile() {
        return toImmutable();
    }

    @Override
    public RedfileTag getTagForRedfile() {
        return RedfileTags.UNKNOWN;
    }
}
