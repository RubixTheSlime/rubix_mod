package io.github.rubixtheslime.rubix.redfile;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;


public interface TrialFilter {
    boolean test(BlockPos pos);
    boolean test(Entity entity);
    
    default boolean test(Object object) {
        if (object instanceof BlockPos pos) return test(pos);
        if (object instanceof Entity entity) return test(entity);
        return false;
    }

    final class BoxTrialFilter implements TrialFilter {
        private final BlockBox box;

        public BoxTrialFilter(BlockBox box) {
            this.box = box;
        }

        @Override
        public boolean test(BlockPos pos) {
            return box.contains(pos);
        }

        @Override
        public boolean test(Entity entity) {
            return box.contains(entity.getBlockPos());
        }
    }

    final class TrivialTrialFilter implements TrialFilter {
        @Override
        public boolean test(BlockPos pos) {
            return true;
        }

        @Override
        public boolean test(Entity entity) {
            return true;
        }
    }
}
