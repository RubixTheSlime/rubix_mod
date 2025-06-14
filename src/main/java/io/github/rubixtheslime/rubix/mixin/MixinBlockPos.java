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

//    @WrapMethod(method = "offset(Lnet/minecraft/util/math/Direction;)Lnet/minecraft/util/math/BlockPos;")
//    BlockPos offset1Wrap(Direction direction, Operation<BlockPos> original) {
//        return original.call(TransWorldManager.transDirection(direction));
//    }
//
//    @WrapMethod(method = "offset(Lnet/minecraft/util/math/Direction;I)Lnet/minecraft/util/math/BlockPos;")
//    BlockPos offset2Wrap(Direction direction, int i, Operation<BlockPos> original) {
//        return original.call(TransWorldManager.transDirection(direction), i);
//    }
//
//    @WrapMethod(method = "offset(JLnet/minecraft/util/math/Direction;)J")
//    private static long offset3Wrap(long value, Direction direction, Operation<Long> original) {
//        return original.call(value, TransWorldManager.transDirection(direction));
//    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public BlockPos getPosForRedfile() {
        return toImmutable();
    }

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Override
    public RedfileTag getTagForRedfile() {
        return RedfileTags.UNKNOWN;
    }
}
