package io.github.rubixtheslime.rubix.gaygrass;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.awt.image.BufferedImage;
import java.util.List;

import static io.github.rubixtheslime.rubix.gaygrass.PrideFlagManager.BASE_LEVEL;

public abstract class ColorGetter {
    public abstract int getColor(int x, int z);

    public abstract void invalidateCaches();

    public static ColorGetter ofEmpty() {
        return new Empty();
    }

    public static ColorGetter of(List<FlagGetter.Builder> flagBuilders, FlagEntry globalEntry) {
        if (flagBuilders.isEmpty()) return ofEmpty();
        double avgArea = FlagGetter.Builder.avgArea(flagBuilders);
        double expectedCount = globalEntry.get(FlagEntry.DENSITY) * (1L << (BASE_LEVEL * 2)) / avgArea;
        var flagGetter = FlagGetter.of(flagBuilders, expectedCount, globalEntry, BASE_LEVEL, 0);
        return new Actual(flagGetter);
    }

    public abstract void setBiomeSeed(long biomeSeed);

    private static final class Empty extends ColorGetter {
        @Override
        public int getColor(int x, int z) {
            return 0;
        }

        @Override
        public void invalidateCaches() {
        }

        @Override
        public void setBiomeSeed(long biomeSeed) {
        }
    }

    private static final class Actual extends ColorGetter {
        private final FlagGetter flagGetter;

        private final Cache<Long, BufferedImage> tileImageCache = Caffeine.newBuilder()
            .maximumSize(256)
            .build();

        private Actual(FlagGetter flagGetter) {
            this.flagGetter = flagGetter;
        }

        @Override
        public int getColor(int x, int z) {
            int tileX = ((int) x) >> BASE_LEVEL;
            int tileZ = ((int) z) >> BASE_LEVEL;
            var bufferedImage = tileImageCache.get(PrideFlagManager.merge(tileX, tileZ), a -> {
                BufferedImage image = new BufferedImage(1 << BASE_LEVEL, 1 << BASE_LEVEL, BufferedImage.TYPE_INT_ARGB);
                for (var buffer : flagGetter.getBuffers(tileX, tileZ)) {
                    buffer.applyTo(tileX << BASE_LEVEL, tileZ << BASE_LEVEL, image);
                }
                return image;
            });
            int mask = ~(-1 << BASE_LEVEL);
            return bufferedImage.getRGB(((int) x) & mask, ((int) z) & mask);
        }

        @Override
        public void invalidateCaches() {
            tileImageCache.invalidateAll();
            flagGetter.invalidateCaches();
        }

        @Override
        public void setBiomeSeed(long biomeSeed) {
            this.flagGetter.setBiomeSeed(biomeSeed);
        }
    }
}
