package io.github.rubixtheslime.rubix.render;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.rubixtheslime.rubix.RubixMod;
import it.unimi.dsi.fastutil.doubles.*;
import it.unimi.dsi.fastutil.ints.Int2DoubleRBTreeMap;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.apache.commons.math3.special.Erf;
import org.joml.Vector3f;

import java.awt.*;
import java.util.*;
import java.util.List;

public class UncertainPieChart {
    public static final Identifier PIE_CHUNK_TEXTURE = Identifier.of(RubixMod.MOD_ID, "textures/misc/redfile_pie_chunk.png");
    private static final Cache<List<GaussianEntry>, Map<Double, Vector3f>> SEGMENT_CACHE = Caffeine.newBuilder()
        .maximumSize(32)
        .build();
    private static final int SLOTS = 2048;
    private static final int MID_CURVE_POINTS = 8;
    private static final double MAX_CURVE_AMOUNT = 0.2;
    private static final double MIN_OFFSET = 1.000001d / SLOTS;
    private final List<GaussianEntry> entries;

    public UncertainPieChart(List<GaussianEntry> entries) {
        this.entries = entries;
    }

    public static void invalidateCache() {
        SEGMENT_CACHE.invalidateAll();
    }

    public void draw(VertexConsumerProvider vertexConsumerProvider, MatrixStack matrixStack) {
        var builder = vertexConsumerProvider.getBuffer(ModRenderLayer.PIE_CHART_CHUNKS);
        var m = matrixStack.peek();
        var pointsIter = getPoints().entrySet().iterator();
        var prev = pointsIter.next();
        float xPrev = 0.5f;
        float yPrev = 0f;
        var prevPos = prev.getKey();
        var prevColor = colorFromVec(prev.getValue());
        while (pointsIter.hasNext()) {
            var next = pointsIter.next();
            while (prevPos < next.getKey()) {
                var nextPos = Math.min(prevPos + MAX_CURVE_AMOUNT, next.getKey());
                var nextColorVec = new Vector3f(prev.getValue());
                float progress = (float) ((nextPos - prev.getKey()) / (next.getKey() - prev.getKey()));
                nextColorVec.mul(1 - progress);
                var tmp = new Vector3f(next.getValue());
                tmp.mul(progress);
                nextColorVec.add(tmp);
                var nextColor = colorFromVec(nextColorVec);
                float x = (float) (1 + Math.sin(nextPos)) * 0.5f;
                float y = (float) (1 - Math.cos(nextPos)) * 0.5f;
                var c = prevColor;
                for (int i = 0; i < 2; i++) {
                    builder.vertex(m, 0.5f, 0.5f, 10).texture(i, i).color(c);
                    builder.vertex(m, xPrev, yPrev, 10).texture(1, 0).color(c);
                    builder.vertex(m, x, y, 10).texture(0, 1).color(c);
                    c = nextColor;
                }

                prevPos = nextPos;
                prevColor = nextColor;
                xPrev = x;
                yPrev = y;
            }
            prev = next;
        }
    }

    private static int colorFromVec(Vector3f vec) {
        return ColorHelper.fromFloats(1f, (float) Math.sqrt(vec.x), (float) Math.sqrt(vec.y), (float) Math.sqrt(vec.z));
    }


    private Map<Double, Vector3f> getPoints() {
        if (entries.size() == 1) {
            var color = entries.getFirst().color.getRGB();
            var r = ColorHelper.getRedFloat(color);
            var g = ColorHelper.getGreenFloat(color);
            var b = ColorHelper.getBlueFloat(color);
            var v = new Vector3f(r * r, g * g, b * b);
            return new Double2ObjectArrayMap<>(new double[]{0d, Math.PI * 2}, new Vector3f[]{v, v}, 2);
        }
        return SEGMENT_CACHE.get(entries, entries -> {
            ArrayDeque<List<ColorBuilderEntry>> deque = new ArrayDeque<>(entries.size());
            var scale = 1 / entries.stream().mapToDouble(GaussianEntry::mean).sum();
            double cumulative = 0;
            for (var entry : entries) {
                // we want to fill the builder with "representative" values. in this case, that is where lerping between them
                // looks very similar to the actual curve. note that the values are the complement of normal cmf of half size
                // stretching out in both directions. so going in both directions, we put the min (center of the slice), mode
                // (what would be the edge if it were not fuzzy), and the max (opposite the center). between those three points
                // we interpolate by looking at the pmf, and lerping along the y axis. that is, we take the derivative of
                // the function we try to imitate, then wherever that derivative is itself changing quickly we have many
                // samples, but few where the derivative is relatively still, very conducive for lerping between the points.
                // we also put a sample at 0 and 1 (the point where it loops), so that when we merge them all together, it
                // remains continuous at the loop point.

                SortedMap<Integer, Double> builder = new Int2DoubleRBTreeMap();
                double offsetMean = scale * 0.5 * entry.mean;
                double offsetDev = RubixMod.CONFIG.redfileOptions.pieUncertainty() ? scale * 0.5 * entry.stdDev : 0;
                double offsetDevInv =  entry.stdDev == 0 ? Double.POSITIVE_INFINITY : 1d / offsetDev;
                var center = cumulative + offsetMean;
                cumulative += entry.mean * scale;

                double z0 = -offsetMean * offsetDevInv;
                double z1 = (0.5 - offsetMean) * offsetDevInv;
                addEntries(builder, z0, center, offsetMean, offsetDev);
                addEntries(builder, z1, center, offsetMean, offsetDev);

                // handle the special points separately to avoid multiple points that are theoretically overlapping but
                // slightly differ due to floating point logic.
                double endValue = Erf.erfc((Math.min(center, 1 - center) - offsetMean) * offsetDevInv);
                double centerValue = Erf.erfc(z0);
                double oppositeValue = Erf.erfc(z1);
                builder.put(0, endValue);
                putBuilder(builder, center, centerValue);
                putBuilder(builder, center + 0.5, oppositeValue);
                putBuilder(builder, center + offsetMean, 1d);
                putBuilder(builder, center - offsetMean, 1d);

                builder.put(SLOTS, builder.get(0));

                int color = entry.color.getRGB();
                double r = Math.pow(ColorHelper.getRedFloat(color), 2);
                double g = Math.pow(ColorHelper.getGreenFloat(color), 2);
                double b = Math.pow(ColorHelper.getBlueFloat(color), 2);
                deque.add(builder
                    .entrySet()
                    .stream()
                    .map(builderEntry -> new ColorBuilderEntry(builderEntry.getKey(), builderEntry.getValue(), r, g, b))
                    .toList()
                );
            }

            // repeatedly merge the lists together. doing it with the deque ensures they are always similar size. they also
            // naturally start in order so they are similar to surrounding ones. that is to say, it is a little faster due
            // to efficient merging.
            while (deque.size() > 1) {
                var list1 = deque.removeFirst();
                var list2 = deque.removeFirst();
                List<ColorBuilderEntry> res = new ArrayList<>();
                var iter1 = list1.iterator();
                var iter2 = list2.iterator();
                var low = iter1.next();
                var high = iter1.next();
                var mid = iter2.next();
                while (true) {
                    res.add(mid.lerpMerge(low, high));
                    if (!iter2.hasNext()) break;
                    // only handle (near) equal entries once
                    if (mid.position == high.position) {
                        low = high;
                        // if we made it here, iter1 must have a next, their last is the same value (that being 1.0)
                        high = iter1.next();
                    }
                    var prev = mid;
                    mid = iter2.next();
                    if (mid.position < high.position) continue;
                    low = prev;
                    var tmpEntry = mid;
                    mid = high;
                    high = tmpEntry;
                    var tmpIter = iter1;
                    iter1 = iter2;
                    iter2 = tmpIter;
                }
                deque.addLast(res);
            }
            var list = deque.getFirst();
            return new Double2ObjectArrayMap<>(
                list.stream().mapToDouble(ColorBuilderEntry::radialPosition).toArray(),
                list.stream().map(ColorBuilderEntry::getColorVec).toArray(),
                list.size()
            );
        });
    }

    private static void putBuilder(Map<Integer, Double> builder, double x, double y) {
        builder.put(((int) Math.round(x * SLOTS) % SLOTS + SLOTS) % SLOTS, y);
    }

    private static void addEntries(SortedMap<Integer, Double> builder, double maxZ, double center, double offsetMean, double offsetDev) {
        double xScale = (1 - Math.exp(-maxZ * maxZ)) / MID_CURVE_POINTS;
        for (int i = 1; i < MID_CURVE_POINTS; i++) {
            var x = 1 - xScale * i;
            var z = Math.copySign(Math.sqrt(-Math.log(x)), maxZ);
            var offset = offsetMean + (Math.abs(z * offsetDev) < MIN_OFFSET ? MIN_OFFSET * Math.signum(z) : (z * offsetDev));
            var amount = Erf.erfc(z);
            putBuilder(builder, center + offset, amount);
            putBuilder(builder, center - offset, amount);
        }
    }

    private record ColorBuilderEntry(int position, double weight, double r, double g, double b) {
        private ColorBuilderEntry lerpMerge(ColorBuilderEntry low, ColorBuilderEntry high) {
            var x = (double) (this.position - low.position) / (high.position - low.position);
            var lowWeight = low.weight * (1 - x);
            var highWeight = high.weight * x;
            var thisWeight = this.weight;
            var totalWeight = lowWeight + highWeight + thisWeight;
            var totalScale = totalWeight == 0 ? 0 : 1 / totalWeight;
            lowWeight *= totalScale;
            highWeight *= totalScale;
            thisWeight *= totalScale;
            return new ColorBuilderEntry(
                position,
                totalWeight,
                low.r * lowWeight + this.r * thisWeight + high.r * highWeight,
                low.g * lowWeight + this.g * thisWeight + high.g * highWeight,
                low.b * lowWeight + this.b * thisWeight + high.b * highWeight
            );
        }

        private Vector3f getColorVec() {
            return new Vector3f((float) r, (float) g, (float) b);
        }

        public double radialPosition() {
            return Math.PI * 2 / SLOTS * position;
        }
    }

    public record GaussianEntry(Color color, double mean, double stdDev) {
    }


}
