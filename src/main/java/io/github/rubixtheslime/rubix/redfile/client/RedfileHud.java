package io.github.rubixtheslime.rubix.redfile.client;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.rubixtheslime.rubix.RubixMod;
import io.github.rubixtheslime.rubix.imixin.client.IMixinMinecraftClient;
import io.github.rubixtheslime.rubix.redfile.RedfileSummarizer;
import io.github.rubixtheslime.rubix.redfile.RedfileTag;
import io.github.rubixtheslime.rubix.render.ColorMode;
import io.github.rubixtheslime.rubix.render.ModRenderLayer;
import io.github.rubixtheslime.rubix.render.UncertainPieChart;
import io.github.rubixtheslime.rubix.util.MeanAndVar;
import io.github.rubixtheslime.rubix.util.MoreMath;
import io.github.rubixtheslime.rubix.util.Util;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;

import java.awt.*;
import java.util.*;
import java.util.List;

public class RedfileHud {
    private static final Cache<List<RedfileTag>, Map<RedfileTag, Integer>> COLOR_CACHE = Caffeine.newBuilder()
        .maximumSize(32)
        .build();

    private static final int CHART_WIDTH = 100;
    private static final int CHART_HEIGHT = 100;
    private static final int TEXT_PAD = 10;

    private static final double MOTION_SCALE = 0.1;
    private static final double SPRING_FACTOR = 1;
    private static final double DISPLACE_FACTOR = 0.1;
    private static final double MAX_DISPLACE = 1;
    private static final double STABLE_THRESHOLD = 0.0001;
    private static final int MAX_MOTION_ITERATIONS = 64;

    public static void init() {
        RubixMod.CONFIG.redfileOptions.subscribeToPieColorMode(x -> invalidateCaches());
        RubixMod.CONFIG.redfileOptions.subscribeToPieUncertainty(x -> invalidateCaches());
        RubixMod.CONFIG.redfileOptions.subscribeToUncertaintyMode(x -> {
            invalidateCaches();
            var manager = ((IMixinMinecraftClient)MinecraftClient.getInstance()).rubix$getRedfileResultManager();
            manager.invalidateSum();
        });
    }

    private static void invalidateCaches() {
        COLOR_CACHE.invalidateAll();
        UncertainPieChart.invalidateCache();
    }

    public static void renderRedfileHud(DrawContext context, MinecraftClient client, RenderTickCounter tickCounter) {
        var manager = ((IMixinMinecraftClient) client).rubix$getRedfileResultManager();
        var lookingAt = manager.getLookingAt(client);
        var blockStats = lookingAt == null ? null : lookingAt.stats();
        var sumStats = manager.getSumOfSelected(client.world);
        double confidence = 1d / 100 * RubixMod.CONFIG.redfileOptions.confidence();
        boolean displayConfidence = RubixMod.CONFIG.redfileOptions.displayConfidenceHud();
        var stats = sumStats == null ? blockStats : sumStats;
        if (stats == null || stats.isEmpty()) return;
        List<RedfileRenderEntry> renderEntries = new ArrayList<>();

        if (blockStats != null && !blockStats.isEmpty()) renderEntries.addLast(new RedfileRenderEntry(Color.white, Text.translatable("rubix.hud.redfile.block"), blockStats.sum(), confidence, displayConfidence, false));
        if (sumStats != null && !sumStats.isEmpty()) renderEntries.addLast(new RedfileRenderEntry(Color.white, Text.translatable("rubix.hud.redfile.sum"), sumStats.sum(), confidence, displayConfidence, false));
        if (stats.shouldBreakDown()) {
            List<RedfileTag> tagsInOrder = stats.data().keySet().stream().toList();
            var colors = getColors(tagsInOrder);
            var colorIter = colors.iterator();
            for (var entry : stats.data().entrySet()) {
                var color = colorIter.next();
                var name = entry.getKey().getName();
                var mv = entry.getValue();
                renderEntries.addLast(new RedfileRenderEntry(color, name, mv, confidence, false, true));
            }
        }

        var pieChartEntries = renderEntries.stream()
            .filter(RedfileRenderEntry::includeInPieChart)
            .map(entry -> new UncertainPieChart.GaussianEntry(entry.color, entry.mv.mean(), entry.mv.stdDev()))
            .toList();
        var pieChart = pieChartEntries.isEmpty() ? null : new UncertainPieChart(pieChartEntries);

        var textRenderer = MinecraftClient.getInstance().textRenderer;
        int yTextOffset = pieChart == null ? 0 : CHART_HEIGHT + 5;
        int hudHeight = renderEntries.size() * 9 + yTextOffset;
        int nameTextWidth = renderEntries.stream().map(entry -> textRenderer.getWidth(entry.name)).max(Integer::compareTo).orElse(0);
        int amountTextWidth = renderEntries.stream().map(entry -> textRenderer.getWidth(entry.mvText)).max(Integer::compareTo).orElse(0);
        int textWidth = nameTextWidth + TEXT_PAD + amountTextWidth;
        int chartWidth = pieChart == null ? 0 : CHART_WIDTH;
        int hudWidth = Math.max(textWidth, chartWidth);
        int textOffset = Math.max(hudWidth - textWidth, 0) / 2;
        int chartOffset = Math.max(hudWidth - chartWidth, 0) / 2;
        int x = context.getScaledWindowWidth() - hudWidth - 10;
        int y = context.getScaledWindowHeight() - hudHeight - 10;

        context.draw(vertexConsumers -> {
            var builder = vertexConsumers.getBuffer(ModRenderLayer.PIE_CHART_CHUNKS);
            builder.vertex(x - 5, y - 5, 9).color(0x7f222222).texture(0, 0);
            builder.vertex(x + 5 + hudWidth, y - 5, 9).color(0x7f222222).texture(0, 0);
            builder.vertex(x + 5 + hudWidth, y + 5 + hudHeight, 9).color(0x7f222222).texture(0, 0);
            builder.vertex(x + 5 + hudWidth, y + 5 + hudHeight, 9).color(0x7f222222).texture(0, 0);
            builder.vertex(x - 5, y + 5 + hudHeight, 9).color(0x7f222222).texture(0, 0);
            builder.vertex(x - 5, y - 5, 9).color(0x7f222222).texture(0, 0);
        });

        if (pieChart != null) {
            context.draw(vertexConsumers -> {
                var matrices = context.getMatrices();
                matrices.push();
                matrices.translate(x + chartOffset, y, 0);
                matrices.scale(CHART_WIDTH, CHART_HEIGHT, 1);
                pieChart.draw(vertexConsumers, matrices);
                matrices.pop();
            });
        }
        int yText = y + yTextOffset;
        int xName = x + textOffset;
        int xMv = x + textOffset + nameTextWidth + TEXT_PAD;
        for (var entry : renderEntries) {
            context.drawText(textRenderer, entry.name, xName, yText, entry.color.getRGB(), false);
            context.drawText(textRenderer, entry.mvText, xMv, yText, entry.color.getRGB(), false);
            yText += 9;
        }
    }

    private static List<Color> getColors(List<RedfileTag> tagsInOrder) {
        var key = new ArrayList<>(tagsInOrder);
        key.sort(Comparator.comparing(RedfileTag::index));

        var map = COLOR_CACHE.get(key, list -> {
            ColorMode colorMode = RubixMod.CONFIG.redfileOptions.pieColorMode();
            var homes = list.stream().map(tag -> tag.getInitialPoint(colorMode)).toList();
            var points = new ArrayList<>(homes);
            var movements = new ArrayList<>(homes);
            movements.replaceAll(x -> new double[colorMode.components]);
            var origin = new double[colorMode.components];
            var old = new double[colorMode.components];
            Set<double[]> uniques = new ObjectOpenHashSet<>(colorMode.components);
            var random = new Xoroshiro128PlusPlusRandom(0);
            for (int iteration = 0; iteration < MAX_MOTION_ITERATIONS; iteration++) {
                var homeIter = homes.iterator();
                var movementIter = movements.iterator();
                double totalMovement = 0;
                for (var point : points) {
                    var home = homeIter.next();
                    var movement = movementIter.next();
                    for (int i = 0; i < colorMode.components; i++) {
                        movement[i] = (home[i] - point[i]) * SPRING_FACTOR;
                    }
                    for (var other : points) {
                        var sqDistance = sqDistance(point, other);
                        if (sqDistance == 0) continue;
                        var scale = Math.pow(sqDistance, -1.5);
                        for (int i = 0; i < colorMode.components; i++) {
                            movement[i] += (point[i] - other[i]) * scale;
                        }
                    }
                    totalMovement += distance(movement, origin);
                }
                if (totalMovement == 0) break;
                double motionScale = MOTION_SCALE / totalMovement;
                double totalActualMotion = 0;
                movementIter = movements.iterator();
                for (var point : points) {
                    var movement = movementIter.next();
                    for (int i = 0; i < colorMode.components; i++) {
                        old[i] = point[i];
                        point[i] += movement[i] * motionScale;
                    }
                    while (uniques.contains(point)) {
                        for (int i = 0; i < colorMode.components; i++) {
                            point[i] += Math.clamp(random.nextGaussian() * DISPLACE_FACTOR, -MAX_DISPLACE, MAX_DISPLACE);
                        }
                    }
                    uniques.add(point);
                    var sqDistance = sqDistance(point, origin);
                    if (sqDistance > 1) {
                        double scale = Math.pow(sqDistance, -0.5);
                        for (int i = 0; i < colorMode.components; i++) {
                            point[i] *= scale;
                        }
                    }
                    totalActualMotion += distance(point, old);
                }
                uniques.clear();
                if (totalActualMotion < STABLE_THRESHOLD) break;
            }
            var tagIter = list.iterator();
            Map<RedfileTag, Integer> resMap = new Reference2IntOpenHashMap<>(list.size());
            for (var point : points) {
                for (int i = 0; i < colorMode.components; i++) {
                    point[i] = (point[i] + 1) * 0.5;
                }
                var tag = tagIter.next();
                resMap.put(tag, colorMode.fromComponents(point));
            }
            return resMap;
        });
        assert map != null;
        return tagsInOrder.stream().map(tag -> new Color(map.get(tag))).toList();
    }

    private static double distance(double[] u, double[] v) {
        return Math.sqrt(sqDistance(u, v));
    }

    private static double sqDistance(double[] u, double[] v) {
        double res = 0;
        for (int i = 0; i < u.length; i++) {
            double x = u[i] - v[i];
            res += x * x;
        }
        return res;
    }

    private record RedfileRenderEntry(Color color, Text name, MeanAndVar mv, Text mvText, boolean displayConfidence, boolean includeInPieChart) {
        RedfileRenderEntry(Color color, Text name, MeanAndVar mv, double confidence, boolean displayConfidence, boolean includeInPieChart) {
            this(color, name, mv, getText(mv, confidence, displayConfidence), displayConfidence, includeInPieChart);
        }

        private static Text getText(MeanAndVar mv, double confidence, boolean displayConfidence) {
            var sumInterval = MoreMath.clampZero(mv.middleInterval(confidence));
            return Util.formatTimeInterval(RedfileSummarizer.CompareMode.RANGE, sumInterval, displayConfidence, false);
        }

    }

}
