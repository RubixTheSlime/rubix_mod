package io.github.rubixtheslime.rubix.util;

import net.minecraft.util.StringIdentifiable;

import java.util.Set;

public enum SetOperation implements StringIdentifiable {
    ASSIGN("assign") {
        @Override
        public long getLong(long a, long b) {
            return b;
        }

        @Override
        public boolean getBool(boolean a, boolean b) {
            return b;
        }

        @Override
        public <T> void applyToSet(Set<T> a, Set<T> b) {
            SetOps.wrap(a).replace(b);
        }

        @Override
        public <T> Set<T> getSet(Set<T> a, Set<T> b) {
            return b;
        }
    },
    UNION("union") {
        @Override
        public long getLong(long a, long b) {
            return a | b;
        }

        @Override
        public boolean getBool(boolean a, boolean b) {
            return a || b;
        }

        @Override
        public <T> void applyToSet(Set<T> a, Set<T> b) {
            a.addAll(b);
        }

        @Override
        public <T> Set<T> getSet(Set<T> a, Set<T> b) {
            return SetOps.wrap(a).getUnion(b);
        }
    },
    INTERSECT("intersect") {
        @Override
        public long getLong(long a, long b) {
            return a & b;
        }

        @Override
        public boolean getBool(boolean a, boolean b) {
            return a && b;
        }

        @Override
        public <T> void applyToSet(Set<T> a, Set<T> b) {
            a.retainAll(b);
        }

        @Override
        public <T> Set<T> getSet(Set<T> a, Set<T> b) {
            return SetOps.wrap(a).getIntersect(b);
        }
    },
    DIFF("diff") {
        @Override
        public long getLong(long a, long b) {
            return a & ~b;
        }

        @Override
        public boolean getBool(boolean a, boolean b) {
            return a && !b;
        }

        @Override
        public <T> void applyToSet(Set<T> a, Set<T> b) {
            a.removeAll(b);
        }

        @Override
        public <T> Set<T> getSet(Set<T> a, Set<T> b) {
            return SetOps.wrap(a).getDiff(b);
        }
    },
    SYMDIFF("symdiff") {
        @Override
        public long getLong(long a, long b) {
            return a ^ b;
        }

        @Override
        public boolean getBool(boolean a, boolean b) {
            return a ^ b;
        }

        @Override
        public <T> void applyToSet(Set<T> a, Set<T> b) {
            b.forEach(x -> {
                if (!a.remove(x)) a.add(x);
            });
        }

        @Override
        public <T> Set<T> getSet(Set<T> a, Set<T> b) {
            return SetOps.wrap(a).getSymdiff(b);
        }
    };

    private final String name;

    SetOperation(String name) {
        this.name = name;
    }

    public abstract long getLong(long a, long b);
    public abstract boolean getBool(boolean a, boolean b);
    public abstract <T> void applyToSet(Set<T> a, Set<T> b);

    public abstract <T> Set<T> getSet(Set<T> a, Set<T> b);

    public int getInt(int a, int b) {
        return (int) getLong(a, b);
    }

    @Override
    public String asString() {
        return name;
    }
}
