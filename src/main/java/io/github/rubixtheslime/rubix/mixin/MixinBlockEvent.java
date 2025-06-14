package io.github.rubixtheslime.rubix.mixin;

import io.github.rubixtheslime.rubix.redfile.RedfileTag;
import io.github.rubixtheslime.rubix.redfile.RedfileTags;
import io.github.rubixtheslime.rubix.redfile.RedfileTracker;
import net.minecraft.server.world.BlockEvent;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockEvent.class)
public class MixinBlockEvent implements RedfileTracker {

    @Shadow @Final private BlockPos pos;

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public BlockPos getPosForRedfile() {
        return pos.toImmutable();
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public RedfileTag getTagForRedfile() {
        return RedfileTags.BLOCK_EVENT;
    }


}
