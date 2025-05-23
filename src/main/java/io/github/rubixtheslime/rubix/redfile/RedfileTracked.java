package io.github.rubixtheslime.rubix.redfile;

import net.minecraft.util.math.BlockPos;

public interface RedfileTracked {
    Empty EMPTY = new RedfileTracked.Empty();

    BlockPos rubix$getPosForRedfile();

    final class Empty implements RedfileTracked {
        @Override
        public BlockPos rubix$getPosForRedfile() {
            return null;
        }
    }
}
