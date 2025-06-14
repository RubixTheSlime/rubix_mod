package io.github.rubixtheslime.rubix.gaygrass;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.rubixtheslime.rubix.EnabledMods;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.noise.PerlinNoiseSampler;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class ColorGetter {
    public static final int BASE_LEVEL_INDEX = 4;
    public static final int BASE_LEVEL = 1 << BASE_LEVEL_INDEX;
    public static final long DOUBLE_BASE_LEVEL = 1L << (BASE_LEVEL_INDEX * 2);
    public static final int BASE_LEVEL_MASK = BASE_LEVEL - 1;

    protected ColorGetter() {
    }

    public abstract int getColor(int x, int z);
    public abstract boolean isAnimated(int x, int z);

    public abstract void invalidateCaches();
    public abstract void invalidateTileCache();

    public abstract double getRotate(double x, double y, double damp);

    protected abstract Map<Identifier, FlagBuffer.Animated> getAnimatedBuffers();

    public static ColorGetter ofEmpty() {
        return new Empty();
    }

    public static ColorGetter of(List<FlagGetter.Builder> flagBuilders, JsonFlagEntry globalEntry) {
        if (flagBuilders.isEmpty()) return ofEmpty();
        Map<Identifier, FlagBuffer.Animated> map = new Object2ObjectOpenHashMap<>();
        for (var builder : flagBuilders) {
            var data = builder.flagData;
            if (data.buffer instanceof FlagBuffer.Animated animated) {
                map.put(data.identifier, animated);
            }
        }
        double avgArea = FlagGetter.Builder.avgArea(flagBuilders);
        double expectedCount = globalEntry.get(JsonFlagEntry.DENSITY) * DOUBLE_BASE_LEVEL / avgArea;
        PerlinNoiseSampler[] samplers = new PerlinNoiseSampler[4];
        var flagGetter = FlagGetter.of(flagBuilders, expectedCount, globalEntry, samplers, BASE_LEVEL_INDEX, EnabledMods.GAY_GRASS_VIDEO ? 64 : 0);
        return new Actual(flagGetter, map);
    }

    public abstract void setBiomeSeed(long biomeSeed);

    public void applyToAnimated(String idStr, Consumer<FlagBuffer.Animated> f) {
        if (Objects.equals(idStr, "*")) {
            getAnimatedBuffers().values().forEach(f);
            return;
        }
        var buffer = getAnimatedBuffers().get(Identifier.of(idStr));
        if (buffer != null) f.accept(buffer);
    }

    public Stream<Identifier> getAnimatedNames() {
        return getAnimatedBuffers().keySet().stream();
    }

    private static final class Empty extends ColorGetter {
        private Empty() {
            super();
        }

        @Override
        public int getColor(int x, int z) {
            return 0;
        }

        @Override
        public boolean isAnimated(int x, int z) {
            return false;
        }

        @Override
        public void invalidateCaches() {
        }

        @Override
        public void invalidateTileCache() {
        }

        @Override
        public double getRotate(double x, double y, double damp) {
            return 0;
        }

        @Override
        protected Map<Identifier, FlagBuffer.Animated> getAnimatedBuffers() {
            return Map.of();
        }

        @Override
        public void setBiomeSeed(long biomeSeed) {
        }
    }

    private static final class Actual extends ColorGetter {
        private final Map<Identifier, FlagBuffer.Animated> animatedBuffers;
        private final FlagGetter flagGetter;

        private final Cache<Long, BitSet> isAnimatedCache = EnabledMods.GAY_GRASS_VIDEO ? Caffeine.newBuilder()
            .maximumSize(64)
            .build() : null;
        private final Cache<Long, BufferedImage> tileImageCache = Caffeine.newBuilder()
            .maximumSize(64)
            .build();

        private Actual(FlagGetter flagGetter, Map<Identifier, FlagBuffer.Animated> animatedBuffers) {
            super();
            this.flagGetter = flagGetter;
            this.animatedBuffers = animatedBuffers;
        }

        private BufferedImage getImage(int tileX, int tileZ, FlagInstance.AnimationKey animationKey) {
            BufferedImage image = new BufferedImage(BASE_LEVEL, BASE_LEVEL, BufferedImage.TYPE_INT_ARGB);
            for (var buffer : flagGetter.getBuffers(tileX, tileZ)) {
                buffer.applyTo(tileX << BASE_LEVEL_INDEX, tileZ << BASE_LEVEL_INDEX, image, animationKey);
            }
            return image;
        }

        @Override
        public int getColor(int x, int z) {
            int tileX = x >> BASE_LEVEL_INDEX;
            int tileZ = z >> BASE_LEVEL_INDEX;
            var bufferedImage = tileImageCache.get(PrideFlagManager.merge(tileX, tileZ), a -> getImage(tileX, tileZ, FlagInstance.AnimationKey.ACTUAL));
            assert bufferedImage != null;
            return bufferedImage.getRGB(x & BASE_LEVEL_MASK, z & BASE_LEVEL_MASK);
        }

        @Override
        public boolean isAnimated(int x, int z) {
            if (isAnimatedCache == null) return false;
            int tileX = x >> BASE_LEVEL_INDEX;
            int tileZ = z >> BASE_LEVEL_INDEX;
            var bitset = isAnimatedCache.get(PrideFlagManager.merge(tileX, tileZ), a -> {
                var image = getImage(tileX, tileZ, FlagInstance.AnimationKey.ACTUAL);
                var blackImage = getImage(tileX, tileZ, FlagInstance.AnimationKey.BLACK);
                var whiteImage = getImage(tileX, tileZ, FlagInstance.AnimationKey.WHITE);
                var res = new BitSet();
                for (int i = 0; i < DOUBLE_BASE_LEVEL; i++) {
                    int ix = i >> BASE_LEVEL_INDEX;
                    int iy = i & BASE_LEVEL_MASK;
                    res.set(i, image.getRGB(ix, iy) != 0 && blackImage.getRGB(ix, iy) != whiteImage.getRGB(ix, iy));
                }
                return res;
            });
            assert bitset != null;
            return bitset.get((x & BASE_LEVEL_MASK) << BASE_LEVEL_INDEX | z & BASE_LEVEL_MASK);

        }

        @Override
        public void invalidateCaches() {
            tileImageCache.invalidateAll();
            if (isAnimatedCache != null) isAnimatedCache.invalidateAll();
            flagGetter.invalidateCaches();
        }

        @Override
        public void invalidateTileCache() {
            tileImageCache.invalidateAll();
        }

        @Override
        public double getRotate(double x, double y, double damp) {
            return flagGetter.getPerlinRotate(x, y, damp);
        }

        @Override
        protected Map<Identifier, FlagBuffer.Animated> getAnimatedBuffers() {
            return animatedBuffers;
        }

        @Override
        public void setBiomeSeed(long biomeSeed) {
            this.flagGetter.setBiomeSeed(biomeSeed);
        }

    }

}
