package io.github.rubixtheslime.rubix.redfile;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;


public interface TrialFilter {
    boolean test(RedfileTag tag, BlockPos pos);

    final class BoxTrialFilter implements TrialFilter {
        private final BlockBox box;

        public BoxTrialFilter(BlockBox box) {
            this.box = box;
        }

        @Override
        public boolean test(RedfileTag tag, BlockPos pos) {
            return box.contains(pos);
        }
    }

    final class TrivialTrialFilter implements TrialFilter {
        @Override
        public boolean test(RedfileTag tag, BlockPos pos) {
            return true;
        }
    }
}
