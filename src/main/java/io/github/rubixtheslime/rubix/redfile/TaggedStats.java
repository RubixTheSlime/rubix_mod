package io.github.rubixtheslime.rubix.redfile;

import io.github.rubixtheslime.rubix.ModRegistries;
import io.github.rubixtheslime.rubix.util.MoreMath;
import io.github.rubixtheslime.rubix.util.Util;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TaggedStats {
    private final Map<RedfileTag, MoreMath.MeanAndVar> data;

    private TaggedStats(Map<RedfileTag, MoreMath.MeanAndVar> data) {
        this.data = data;
    }

    public static TaggedStats create(boolean sync) {
        return new TaggedStats(sync ? new ConcurrentHashMap<>() : new Reference2ObjectOpenHashMap<>());
    }

    public static TaggedStats of(Map<RedfileTag, MoreMath.MeanAndVar> data) {
        return new TaggedStats(data);
    }

    public static TaggedStats unpack(int[] data, boolean sync, List<RedfileTag> translation) {
        var res = create(sync);
        if (data == null) return res;
        for (int ptr = 0; ptr < data.length; ptr += 3) {
            var tag = ModRegistries.REDFILE_TAG.get(data[ptr]);
            res.data.merge(
                tag == null ? RedfileTags.UNFAMILIAR : tag,
                MoreMath.MeanAndVar.unpack(data[ptr + 1] | ((long) data[ptr + 2]) << 32),
                (a, b) -> {
                    var res1 = a.copy();
                    res1.add(b);
                    return res1;
                }
            );
        }
        return res;
    }

    public static TaggedStats unpack(int[] data, boolean sync) {
        var res = create(sync);
        if (data == null) return res;
        for (int ptr = 0; ptr < data.length; ptr += 3) {
            res.data.put(
                ModRegistries.REDFILE_TAG.get(data[ptr]),
                MoreMath.MeanAndVar.unpack(data[ptr + 1] | ((long) data[ptr + 2]) << 32)
            );
        }
        return res;
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
        if (tags == null) {
            data.values().forEach(acc::add);
        } else {
            tags.stream().map(data::get).forEach(acc::add);
        }
        MoreMath.MeanAndVar mvSum = acc.finish();
        Map<RedfileTag, MoreMath.MeanAndVar> finalData = new Reference2ObjectArrayMap<>(data.size());
        data.entrySet().stream()
            .filter(entry -> tags == null || tags.contains(entry.getKey()))
            .sorted(Comparator.comparing(entry -> -entry.getValue().mean()))
            .forEach(entry -> finalData.put(entry.getKey(), entry.getValue()));
        return new Display(mvSum, finalData, this);
    }

    public MoreMath.MeanAndVar get(RedfileTag tag) {
        return data.getOrDefault(tag, new MoreMath.MeanAndVar());
    }

    public void finishCollecting(long count) {
        Util.removeIfValue(data, MoreMath.MeanAndVar::isEmpty);
        data.forEach((k, v) -> v.finishPredictive(count));
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public record Display(MoreMath.MeanAndVar sum, Map<RedfileTag, MoreMath.MeanAndVar> data, TaggedStats stats) {
        public boolean isOnlyUntagged() {
            return data().size() == 1 && data().containsKey(RedfileTags.UNTAGGED);
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
                stats.data.computeIfAbsent(entry.getKey(), a -> new MoreMath.MeanAndVar())
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
            stats.data.computeIfAbsent(RedfileTags.UNTAGGED, a -> new MoreMath.MeanAndVar())
                .update(count * sampleWeight, trialCount);
            count = 0;
        }
    }

}
