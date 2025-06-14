package io.github.rubixtheslime.rubix.mixin;

import io.github.rubixtheslime.rubix.redfile.RedfileTag;
import io.github.rubixtheslime.rubix.redfile.RedfileTags;
import io.github.rubixtheslime.rubix.redfile.RedfileTracker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockEntityTickInvoker.class)
public interface MixinBlockEntityTickInvoker extends RedfileTracker {
    @Shadow BlockPos getPos();

    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    default BlockPos getPosForRedfile() {
        return getPos().toImmutable();
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    default RedfileTag getTagForRedfile() {
        return RedfileTags.TILE_ENTITY_TICK;
    }
}
