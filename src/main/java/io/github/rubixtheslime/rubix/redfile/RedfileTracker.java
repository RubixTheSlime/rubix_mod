package io.github.rubixtheslime.rubix.redfile;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;

import java.util.List;
import java.util.function.Function;

public interface RedfileTracker {
    Empty EMPTY = new RedfileTracker.Empty();

    BlockPos getPosForRedfile();

    RedfileTag getTagForRedfile();

    class Empty implements RedfileTracker {
        @Override
        public BlockPos getPosForRedfile() {
            return null;
        }

        @Override
        public RedfileTag getTagForRedfile() {
            return null;
        }
    }

    abstract class BaseTracker implements RedfileTracker {
        private final RedfileTag tag;

        protected BaseTracker(RedfileTag tag) {
            this.tag = tag;
        }

        @Override
        public RedfileTag getTagForRedfile() {
            return tag;
        }
    }

    abstract class Convert<T> extends BaseTracker {
        private T current;

        protected Convert(RedfileTag tag) {
            super(tag);
        }

        @Override
        public BlockPos getPosForRedfile() {
            return convert(current);
        }

        public void set(T t) {
            current = t;
        }

        public void enter(T t) {
            set(t);
            RedfileManager.enter(this);
        }

        public void enterLeaf(T t) {
            set(t);
            RedfileManager.enterLeaf(this);
        }

        protected abstract BlockPos convert(T t);
    }

    class Simple extends Convert<BlockPos> {
        protected Simple(RedfileTag tag) {
            super(tag);
        }

        public static Simple of(RedfileTag tag) {
            return new Simple(tag);
        }

        @Override
        public BlockPos convert(BlockPos pos) {
            return pos.toImmutable();
        }
    }

    class ListRandom extends Convert<List<?>> {
        private static final Random random = new Xoroshiro128PlusPlusRandom(0);

        protected ListRandom(RedfileTag tag) {
            super(tag);
        }

        public static ListRandom of(RedfileTag tag) {
            return new ListRandom(tag);
        }

        public static ListRandom of(RedfileTag tag, List<?> list) {
            var res = new ListRandom(tag);
            res.set(list);
            return res;
        }

        @Override
        public BlockPos convert(List<?> list) {
            if (list == null) return null;
            try {
                int size = list.size();
                if (size == 0) return null;
                return ((RedfileTracker) list.get(random.nextInt(size))).getPosForRedfile();
            } catch (IndexOutOfBoundsException ignored) {
                // in case it was modified between checking the size and getting the item
                return null;
            }
        }

    }

//    class RandomTrackedList implements RedfileTracked {
//        private final List<?> list;
//
//        public RandomTrackedList(List<?> list) {
//            this.list = list;
//        }
//
//        @Override
//        public BlockPos rubix$getPosForRedfile() {
//        }
//    }

}
