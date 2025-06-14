package io.github.rubixtheslime.rubix.mixin;

import io.github.rubixtheslime.rubix.redfile.RedfileTag;
import io.github.rubixtheslime.rubix.redfile.RedfileTags;
import io.github.rubixtheslime.rubix.redfile.RedfileTracker;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockHitResult.class)
public class MixinBlockHitResult implements RedfileTracker {
    @Shadow @Final private BlockPos blockPos;

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public BlockPos getPosForRedfile() {
        return blockPos;
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public RedfileTag getTagForRedfile() {
        return RedfileTags.BLOCK_INTERACT;
    }
}
