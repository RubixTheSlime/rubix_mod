package io.github.rubixtheslime.rubix.gaygrass;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

public class FlagGetter {
    private final WeightedRandomGetter<Entry> entryGetter;
    private final double expectedCount;
    private final long paprika;
    private long seed;
    private final int level;
    private final FlagGetter parent;
    private final PerlinNoiseSampler[] perlinNoiseSamplers;

    private FlagGetter(WeightedRandomGetter<Entry> entryGetter, double expectedCount, long paprika, int level, FlagGetter parent, PerlinNoiseSampler[] perlinNoiseSamplers) {
        this.entryGetter = entryGetter;
        this.expectedCount = expectedCount;
        this.level = level;
        this.parent = parent;
        this.paprika = paprika;
        this.perlinNoiseSamplers = perlinNoiseSamplers;
    }

    public void setBiomeSeed(long biomeSeed) {
        setBiomeSeedInner(biomeSeed);
        Random random = getRandom(seed);
        for (int i = 0; i < perlinNoiseSamplers.length; i++) {
            perlinNoiseSamplers[i] = new PerlinNoiseSampler(random);
        }
    }

    private void setBiomeSeedInner(long biomeSeed) {
        Random random = getRandom(biomeSeed);
        random.skip(2);
        long levelFactor = random.nextLong() | 1;
        long paprikaFactor = random.nextLong() | 1;
        long newSeed = biomeSeed ^ (levelFactor * (long) level) ^ (paprikaFactor * paprika);
        if (newSeed == this.seed) return;
        this.seed = newSeed;
        setBiomeSeed(biomeSeed);
        this.invalidateCache();
        if (this.parent != null) this.parent.setBiomeSeed(newSeed);
    }

    public static FlagGetter of(List<Builder> builders, double expectedCount, JsonFlagEntry globalEntry, PerlinNoiseSampler[] samplers, int level, int cacheCount) {
        Map<Entry, Double> map = new Reference2DoubleOpenHashMap<>(builders.size());
        builders.forEach(builder -> builder.splitInto(map, level));
        builders.removeIf(builder -> builder.weight <= 0);
        double splitWeight = map.values().stream().mapToDouble(v -> v).sum();
        double remainingWeight = builders.stream().mapToDouble(builder -> builder.weight).sum();
        double splitExpectedCount = expectedCount * splitWeight / (splitWeight + remainingWeight);
        double remainingExpectedCount = expectedCount - splitExpectedCount;
        var entryGetter = WeightedRandomGetter.of(map, Comparator.comparing(a -> a.flagData.identifier));
        var parent = builders.isEmpty() ? null : of(builders, remainingExpectedCount * 4, globalEntry, samplers, level + 1, 64);
        long paprika = globalEntry.get(JsonFlagEntry.PAPRIKA);
        if (cacheCount == 0) return new FlagGetter(entryGetter, splitExpectedCount, paprika, level, parent, samplers);
        return new Cached(entryGetter, splitExpectedCount, paprika, level, parent, samplers, cacheCount);
    }

    private static Random getRandom(long seed) {
        return new Xoroshiro128PlusPlusRandom(seed);
    }

    private void seedRandom(Random random, long tileX, long tileZ) {
        random.setSeed(seed);
        long l = random.nextLong() | 1;
        long m = random.nextLong() | 1;
        long n = (tileX << level) * l ^ (tileZ << level) * m ^ seed;
        random.setSeed(n);
    }

    public List<FlagInstance> getBuffers(int tileX, int tileZ) {
        List<FlagInstance> list;
        var rect = new Rectangle2D.Double(tileX << level, tileZ << level, 1 << level, 1 << level);
        list = new ArrayList<>();
        if (parent != null) {
            for (var buffer : parent.getBuffers(tileX >> 1, tileZ >> 1)) {
                if (buffer.intersects(rect)) {
                    list.add(buffer);
                }
            }
        }

        var newBuffers = new ArrayList<FlagInstance>();
        Random random = getRandom(0);
        for (int checkX = tileX - 1; checkX <= tileX + 1; ++checkX) {
            for (int checkZ = tileZ - 1; checkZ <= tileZ + 1; ++checkZ) {
                seedRandom(random, checkX, checkZ);
                double pmfValue = Math.exp(-expectedCount);
                int successes = 0;
                double luck = random.nextDouble() - pmfValue;
                // test pmfValue > 0 just to make sure we don't get astronomically unlucky and enter an infinite loop due to rounding
                while (luck > 0 && pmfValue > 0) {
                    pmfValue *= expectedCount / ++successes;
                    luck -= pmfValue;
                    Entry base = this.entryGetter.get(random);
                    var buffer = base.makeRandom(random, this, checkX, checkZ);
                    if (buffer.intersects(rect)) {
                        newBuffers.addLast(buffer);
                    }
                }
            }
        }
        newBuffers.sort(FlagInstance::compareZIndex);

        list.addAll(newBuffers);
        return list;
    }

    protected void invalidateCache() {
    }

    public void invalidateCaches() {
        this.invalidateCache();
        if (parent != null) parent.invalidateCaches();
    }

    public double getPerlinRotate(double x, double y, double damp) {
        x /= damp;
        y /= damp;
        double rotationX = getDoublePerlin(x, y, 0);
        double rotationY = getDoublePerlin(x, y, 2);
        return rotationX == 0 && rotationY == 0 ? 0 : Math.atan2(rotationY, rotationX);
    }

    private double getDoublePerlin(double x, double y, int index) {
        return getPerlin(x, y, index) + getPerlin(x + 0.5, y + 0.5, index + 1);
    }

    private double getPerlin(double x, double y, int index) {
        var sampler = perlinNoiseSamplers[index];
        return sampler == null ? 0 : sampler.sample(x, 0, y);
    }

    public static class Cached extends FlagGetter {
        private final Cache<Long, List<FlagInstance>> cache;

        private Cached(WeightedRandomGetter<Entry> flagGetter, double chance, long worldSeed, int level, FlagGetter parent, PerlinNoiseSampler[] samplers, int size) {
            super(flagGetter, chance, worldSeed, level, parent, samplers);
            cache = Caffeine.newBuilder()
                .maximumSize(size)
                .build();
        }

        @Override
        public List<FlagInstance> getBuffers(int tileX, int tileZ) {
            return cache.get(PrideFlagManager.merge(tileX, tileZ), a -> super.getBuffers(tileX, tileZ));
        }

        @Override
        public void invalidateCache() {
            cache.invalidateAll();
        }
    }

    public static class Builder {
        public final FlagData flagData;
        private final AffineTransform baseTransform;
        private double minRadiusScaled;
        private double maxRadiusScaled;
        private double weight;

        public Builder(FlagData flagData, AffineTransform baseTransform, JsonFlagEntry flagEntry) {
            this.flagData = flagData;
            this.baseTransform = baseTransform;
            this.minRadiusScaled = flagData.scale.inv(flagEntry.get(JsonFlagEntry.MIN_SIZE) * 0.5);
            this.maxRadiusScaled = flagData.scale.inv(flagEntry.get(JsonFlagEntry.MAX_SIZE) * 0.5);
            this.weight = flagEntry.get(JsonFlagEntry.WEIGHT);
        }

        public static double avgArea(Collection<Builder> builders) {
            double totalWeight = 0;
            double totalAreaWeight = 0;
            for (var builder : builders) {
                totalWeight += builder.getWeight();
                totalAreaWeight += builder.avgArea() * builder.getWeight();
            }
            return totalAreaWeight / totalWeight;
        }

        public static double getExpectedCount(Collection<Builder> builders, double density, int level) {
            double totalWeight = 0;
            double totalAreaWeight = 0;
            for (var builder : builders) {
                totalWeight += builder.getWeight();
                totalAreaWeight += builder.avgArea() * builder.getWeight();
            }
            double invAvg = totalWeight / totalAreaWeight;
            return density * invAvg * (1L << (level * 2));
        }

        private void splitInto(Map<Entry, Double> entries, int level) {
            double splitPoint = flagData.scale.inv(1L << level);

            if (splitPoint < minRadiusScaled) return;
            if (splitPoint > maxRadiusScaled) {
                entries.put(new Entry(this, minRadiusScaled, maxRadiusScaled, level), weight);
                weight = 0;
                return;
            }
            double splitWeight = weight * (splitPoint - minRadiusScaled) / (maxRadiusScaled - minRadiusScaled);
            entries.put(new Entry(this, minRadiusScaled, splitPoint, level), splitWeight);
            this.minRadiusScaled = splitPoint;
            this.weight -= splitWeight;
        }

        public double avgArea() {
            double w = flagData.buffer.width();
            double h = flagData.buffer.height();
            // integrate over radius & multiply by area per square radius. assumes the svg is a full rectangle
            // `* 2` is to divide squared diagonal to get square radius, but is then halved by the constant in the
            // integral of `exp(2*x)`
            // also divide by the range to make it an average
            return (flagData.scale.integralOfSquared(minRadiusScaled, maxRadiusScaled)) * w * h * 4
                / ((w * w + h * h) * (maxRadiusScaled - minRadiusScaled));
        }

        public double getWeight() {
            return weight;
        }

        public static Builder of(FlagData flagData, JsonFlagEntry flagEntry) throws RuntimeException {
            var flagBuffer = flagData.buffer;
            var identifier = flagData.identifier;
            float scale = 2f / (float) Math.hypot(flagBuffer.width(), flagBuffer.height());
            var transform = AffineTransform.getScaleInstance(scale, scale);
            transform.translate(-flagBuffer.width() / 2, -flagBuffer.height() / 2);
            var scaleImpl = Scale.of(flagEntry.get(JsonFlagEntry.SCALE));
            if (scaleImpl == null) throw new RuntimeException("invalid scale name for %s: %s".formatted(identifier, flagEntry.get(JsonFlagEntry.SCALE)));

            return new Builder(flagData, transform, flagEntry);
        }

        public FlagData getData() {
            return flagData;
        }
    }

    private static class Entry {
        private final FlagData flagData;
        private final AffineTransform baseTransform;
        private final double minRadiusScaled;
        private final double rangeRadiusScaled;
        private final int level;

        private Entry(Builder builder, double minRadiusScaled, double maxRadiusScaled, int level) {
            this.flagData = builder.flagData;
            this.baseTransform = builder.baseTransform;
            this.level = level;
            this.minRadiusScaled = minRadiusScaled;
            this.rangeRadiusScaled = maxRadiusScaled - minRadiusScaled;
        }

        public FlagInstance makeRandom(Random random, FlagGetter getter, long tileX, long tileZ) {
            double translateX = (random.nextDouble() + tileX) * (1 << level);
            double translateZ = (random.nextDouble() + tileZ) * (1 << level);
            double radius = flagData.scale.apply(random.nextDouble() * rangeRadiusScaled + minRadiusScaled);
            double rotation = getter.getPerlinRotate(translateX, translateZ, flagData.rotationDamp);
            Color color;
            if (flagData.randomColorAlpha > 0) {
                color = new Color(ColorHelper.withAlpha(Math.min(255, (int) (flagData.randomColorAlpha * 255)), Color.HSBtoRGB((float) random.nextDouble(), 1, 1)), true);
            } else {
                color = null;
            }
            return make(translateX, translateZ, rotation, radius, color);
        }

        public FlagInstance make(double x, double z, double rotation, double radius, Color color) {
            var transform = AffineTransform.getTranslateInstance(x, z);
            transform.rotate(rotation);
            transform.scale(radius, radius);
            transform.concatenate(baseTransform);
            return new FlagInstance(flagData, transform, radius, color);
        }

    }

}
