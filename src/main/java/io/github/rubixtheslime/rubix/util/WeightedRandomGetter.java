package io.github.rubixtheslime.rubix.util;

import net.minecraft.util.math.random.Random;

import java.util.*;

public class WeightedRandomGetter<T> {
    private final List<T> items;
    private final double[] thresholds;

    private WeightedRandomGetter(List<T> items, double[] weights) {
        this.items = items;
        this.thresholds = weights;
    }

    public static <T> WeightedRandomGetter<T> of(Map<T, Double> map, Comparator<T> comparator) {
        var items = new ArrayList<T>();
        double total = 0;
        for (var entry : map.entrySet()) {
            if (entry.getValue() > 0) {
                items.addLast(entry.getKey());
                total += entry.getValue();
            }
        }
        if (items.isEmpty()) return new WeightedRandomGetter<>(null, null);
        items.sort(comparator);
        var thresholds = new double[items.size() - 1];
        double multiplier = 1 / total;
        double cumWeight = 0;
        for (int i = 0; i < items.size() - 1; i++) {
            cumWeight += map.get(items.get(i)) * multiplier;
            thresholds[i] = cumWeight;
        }
        return new WeightedRandomGetter<>(items, thresholds);
    }

    public T get(Random random) {
        int index = Arrays.binarySearch(thresholds, random.nextDouble());
        index ^= index >> 31;
        return isEmpty() ? null : items.get(index);
    }

    public boolean isEmpty() {
        return items == null;
    }
}
