package io.github.rubixtheslime.rubix.redfile;

import io.github.rubixtheslime.rubix.ModRegistries;
import io.github.rubixtheslime.rubix.util.MeanAndVar;
import io.github.rubixtheslime.rubix.util.MoreMath;
import io.github.rubixtheslime.rubix.util.Util;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class TaggedStats {
    private final Map<RedfileTag, MeanAndVar> data;

    private TaggedStats(Map<RedfileTag, MeanAndVar> data) {
        this.data = data;
    }

    public static TaggedStats create(boolean sync) {
        return new TaggedStats(sync ? new ConcurrentHashMap<>() : new Reference2ObjectOpenHashMap<>());
    }

    public static TaggedStats of(Map<RedfileTag, MeanAndVar> data) {
        return new TaggedStats(data);
    }

    private static TaggedStats unpack(int[] data, boolean sync, Function<Integer, RedfileTag> getter) {
        var res = create(sync);
        if (data == null) return res;
        var unfamiliarAcc = new MoreMath.MeanVarAcc();
        for (int ptr = 0; ptr < data.length; ptr += 3) {
            var tag = getter.apply(data[ptr]);
            var mv = MeanAndVar.unpack(data[ptr + 1] | ((long) data[ptr + 2]) << 32);
            if (tag == null || tag == RedfileTags.UNFAMILIAR) {
                unfamiliarAcc.add(mv);
            } else {
                res.data.put(tag, mv);
            }
        }
        var unfamiliarMv = unfamiliarAcc.finish();
        if (!unfamiliarMv.isEmpty()) {
            res.data.put(RedfileTags.UNFAMILIAR, unfamiliarMv);
        }
        return res;
    }

    public static TaggedStats unpack(int[] data, boolean sync, List<RedfileTag> translation) {
        return unpack(data, sync, translation::get);
    }

    public static TaggedStats unpack(int[] data, boolean sync) {
        return unpack(data, sync, ModRegistries.REDFILE_TAG::get);
    }

    public int[] pack() {
        var res = new int[data.size() * 3];
        int ptr = 0;
        for (var entry : data.entrySet()) {
            res[ptr] = entry.getKey().index();
            long asLong = entry.getValue().pack();
            res[ptr + 1] = (int) asLong;
            res[ptr + 2] = (int) (asLong >>> 32);
            ptr += 3;
        }
        return res;
    }

    public Display getDisplay(Set<RedfileTag> tags) {
        MoreMath.MeanVarAcc acc = new MoreMath.MeanVarAcc();
        MoreMath.MeanVarAcc missingAcc = new MoreMath.MeanVarAcc();
        if (tags == null) {
            data.values().forEach(acc::add);
        } else {
            for (var entry : data.entrySet()) {
                if (tags.contains(entry.getKey())) {
                    acc.add(entry.getValue());
                } else {
                    missingAcc.add(entry.getValue());
                }
            }
        }
        MeanAndVar mvSum = acc.finish();
        MeanAndVar mvMissing = missingAcc.finish();
        Map<RedfileTag, MeanAndVar> finalData = new Reference2ObjectArrayMap<>(data.size());
        data.entrySet().stream()
            .filter(entry -> tags == null || tags.contains(entry.getKey()))
            .sorted(Comparator.comparing(entry -> -entry.getValue().mean()))
            .forEach(entry -> finalData.put(entry.getKey(), entry.getValue()));
        return new Display(mvSum, finalData, mvMissing, this);
    }

    public MeanAndVar get(RedfileTag tag) {
        return data.getOrDefault(tag, new MeanAndVar());
    }

    public void finishCollecting(long count) {
        Util.removeIfValue(data, MeanAndVar::isEmpty);
        data.forEach((k, v) -> v.finishPredictive(count));
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public void multiplyVariance(int amount) {
        data.forEach((k, mv) -> mv.multiplyVariance(amount));
    }

    public void addEmpties(TaggedStats other) {
        other.data.keySet().forEach(key -> data.computeIfAbsent(key, tag -> new MeanAndVar()));
    }

    public record Display(MeanAndVar sum, Map<RedfileTag, MeanAndVar> data, MeanAndVar missing, TaggedStats stats) {
        public boolean shouldBreakDown() {
            return data().size() != 1 || !data().containsKey(RedfileTags.UNTAGGED);
        }

        public boolean isEmpty() {
            return sum.isEmpty();
        }

    }

    public static class Accumulator {
        private final Map<RedfileTag, MoreMath.MeanVarAcc> data = new Reference2ObjectOpenHashMap<>();

        public void add(TaggedStats stats) {
            if (stats == null) return;
            for (var entry : stats.data.entrySet()) {
                data.computeIfAbsent(entry.getKey(), a -> new MoreMath.MeanVarAcc())
                    .add(entry.getValue());
            }
        }

        public TaggedStats finish() {
            var res = TaggedStats.create(false);
            for (var entry : data.entrySet()) {
                res.data.put(entry.getKey(), entry.getValue().finish());
            }
            return res;
        }
    }

    public interface Builder {
        static Builder create(boolean sync, boolean split) {
            return split ? new SplitBuilder(sync ? new ConcurrentHashMap<>() : new Reference2LongOpenHashMap<>()) : new UnsplitBuilder();
        }

        void inc(RedfileTag tag);

        void commit(TaggedStats stats, double sampleWeight, long trialCount);

    }

    private static class SplitBuilder implements Builder {
        private final Map<RedfileTag, Long> data;

        private SplitBuilder(Map<RedfileTag, Long> data) {
            this.data = data;
        }

        @Override
        public void inc(RedfileTag tag) {
            data.merge(tag, 1L, Long::sum);
        }

        @Override
        public void commit(TaggedStats stats, double sampleWeight, long trialCount) {
            for (var entry : data.entrySet()) {
                var value = entry.getValue();
                entry.setValue(0L);
                double x = (double) value * sampleWeight;
                stats.data.computeIfAbsent(entry.getKey(), a -> new MeanAndVar())
                    .update(x, trialCount);
            }
        }
    }

    private static class UnsplitBuilder implements Builder {
        private long count;

        @Override
        public void inc(RedfileTag tag) {
            count++;
        }

        @Override
        public void commit(TaggedStats stats, double sampleWeight, long trialCount) {
            stats.data.computeIfAbsent(RedfileTags.UNTAGGED, a -> new MeanAndVar())
                .update(count * sampleWeight, trialCount);
            count = 0;
        }
    }

}
