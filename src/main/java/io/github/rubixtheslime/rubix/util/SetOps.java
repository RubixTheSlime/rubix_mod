package io.github.rubixtheslime.rubix.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;

public interface SetOps<T> extends Set<T> {
    default void replace(Set<T> other) {
        clear();
        addAll(other);
    }

    default void applySymDiff(Set<T> other) {
        other.forEach(x -> {
            if (!remove(x)) add(x);
        });
    }

    default SetOps<T> getUnion(Set<T> other) {
        return bySize(this, other, wrapped((a, b) -> {
            var res = a.copy();
            res.addAll(b);
            return res;
        }));
    }

    default SetOps<T> getIntersect(Set<T> other) {
        return bySize(this, other, wrapped((a, b) -> {
            var res = a.copy();
            res.retainAll(b);
            return res;
        }));
    }

    default SetOps<T> getDiff(Set<T> other) {
        var res = this.copy();
        res.removeAll(other);
        return res;
    }

    default SetOps<T> getSymdiff(Set<T> other) {
        return bySize(this, other, wrapped((a, b) -> {
            var res = a.copy();
            res.applySymDiff(b);
            return res;
        }));
    }

    default SetOps<T> copy() {
        return wrap(new HashSet<>(this));
    }

    static <T> SetOps<T> wrap(Set<T> set) {
        return set instanceof SetOps<T> res ? res : new Wrapped<>(set);
    }

    static <T, U> Function<Set<T>, U> wrapped(Function<SetOps<T>, U> f) {
        return x -> f.apply(wrap(x));
    }

    static <T, U> BiFunction<Set<T>, Set<T>, U> wrapped(BiFunction<SetOps<T>, SetOps<T>, U> f) {
        return (a, b) -> f.apply(wrap(a), wrap(b));
    }

    static <T> Consumer<Set<T>> wrapped(Consumer<SetOps<T>> f) {
        return x -> f.accept(wrap(x));
    }

    static <T> BiConsumer<Set<T>, Set<T>> wrapped(BiConsumer<SetOps<T>, SetOps<T>> f) {
        return (a, b) -> f.accept(wrap(a), wrap(b));
    }

    static <T, U> U bySize(Set<T> a, Set<T> b, BiFunction<Set<T>, Set<T>, U> f) {
        return a.size() < b.size() ? f.apply(a, b) : f.apply(b, a);
    }

    static <T> void bySize(Set<T> a, Set<T> b, BiConsumer<Set<T>, Set<T>> f) {
        if (a.size() < b.size()) f.accept(a, b);
        else f.accept(b, a);
    }

    class Wrapped<T> implements SetOps<T> {
        private final Set<T> inner;

        public Wrapped(Set<T> inner) {
            this.inner = inner;
        }

        @Override
        public int size() {
            return inner.size();
        }

        @Override
        public boolean isEmpty() {
            return inner.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return inner.contains(o);
        }

        @Override
        public @NotNull Iterator<T> iterator() {
            return inner.iterator();
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            SetOps.super.forEach(action);
        }

        @Override
        public @NotNull Object @NotNull [] toArray() {
            return inner.toArray();
        }

        @Override
        public @NotNull <T1> T1 @NotNull [] toArray(@NotNull T1 @NotNull [] a) {
            return inner.toArray(a);
        }

        @Override
        public <T1> T1[] toArray(@NotNull IntFunction<T1[]> generator) {
            return SetOps.super.toArray(generator);
        }

        @Override
        public boolean add(T t) {
            return inner.add(t);
        }

        @Override
        public boolean remove(Object o) {
            return inner.remove(o);
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            return inner.containsAll(c);
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends T> c) {
            return inner.addAll(c);
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            return inner.retainAll(c);
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            return inner.removeAll(c);
        }

        @Override
        public boolean removeIf(@NotNull Predicate<? super T> filter) {
            return inner.removeIf(filter);
        }

        @Override
        public void clear() {
            inner.clear();
        }

        @Override
        public @NotNull Spliterator<T> spliterator() {
            return inner.spliterator();
        }

        @Override
        public @NotNull Stream<T> stream() {
            return inner.stream();
        }

        @Override
        public @NotNull Stream<T> parallelStream() {
            return inner.parallelStream();
        }

    }
}
