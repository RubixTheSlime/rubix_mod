package io.github.rubixtheslime.rubix.mixin;

import io.github.rubixtheslime.rubix.redfile.RedfileTag;
import io.github.rubixtheslime.rubix.redfile.RedfileTags;
import io.github.rubixtheslime.rubix.redfile.RedfileTracker;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.tick.OrderedTick;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(OrderedTick.class)
public abstract class MixinOrderedTick<T> implements RedfileTracker {

    @Shadow @Final private BlockPos pos;

    @Shadow @Final private T type;

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public BlockPos getPosForRedfile() {
        return pos.toImmutable();
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public RedfileTag getTagForRedfile() {
        return type instanceof Block ? RedfileTags.TILE_TICK_BLOCK : RedfileTags.TILE_TICK_FLUID;
    }
}
