package io.github.rubixtheslime.rubix.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.rubixtheslime.rubix.RubixMod;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.function.Function;

public class UncertainPieChart {
    public static Identifier QUARTER_CIRCLE = Identifier.of(RubixMod.MOD_ID, "textures/misc/quarter_circle.png");
    private final List<? extends IEntry> entries;
    private final int sampleCount;

    public UncertainPieChart(List<? extends IEntry> entries, int sampleCount) {
        this.entries = entries;
        this.sampleCount = sampleCount;
    }

    public void draw(VertexConsumerProvider vertexConsumerProvider, MatrixStack matrixStack) {
        var builder = vertexConsumerProvider.getBuffer(ModRenderLayer.PIE_CHART_CHUNKS);
        var m = matrixStack.peek();
        var random = new Xoroshiro128PlusPlusRandom(0);
        random.skip(3);
//        var mainImage = new BufferedImage(radius * 2, radius * 2, BufferedImage.TYPE_INT_ARGB);
//        var mainGraphics = mainImage.createGraphics();
//        var intImage = new BufferedImage(radius * 2, radius * 2, BufferedImage.TYPE_INT_ARGB);
//        var intGraphics = intImage.createGraphics();
        for (int trial = 1; trial <= sampleCount; trial++) {
            List<Double> checkpoints = new DoubleArrayList(entries.size());
            double cumulative = 0;
            for (var entry : entries) {
                cumulative += Math.max(entry.getSample(random), 0);
                checkpoints.addLast(cumulative);
            }
            if (cumulative == 0) continue;
            double scale = Math.PI * 2 / cumulative;
            checkpoints.replaceAll(x -> x * scale);
            double previous = 0;
            float xPrev = 0;
            float yPrev = -0.5f;
            var checkpointIter = checkpoints.iterator();
            for (var entry : entries) {
                var checkpoint = checkpointIter.next();
                int color = ColorHelper.withAlpha(255 / trial, entry.getColor().getRGB());
                while (previous < checkpoint) {
                    double nextPoint = Math.min(checkpoint, previous + Math.PI / 2);
                    float x = (float) Math.sin(nextPoint) * 0.5f;
                    float y = (float) Math.cos(nextPoint) * -0.5f;
                    float u = (float) Math.cos(nextPoint - previous) * 0.5f;
                    float v = (float) Math.sin(nextPoint - previous);
                    builder.vertex(m, 0.5f, 0.5f, 10).texture(0, 0).color(color);
                    builder.vertex(m, 0.5f + xPrev, 0.5f + yPrev, 10).texture(0.5f, 0).color(color);
                    builder.vertex(m, 0.5f + xPrev + x, 0.5f + yPrev + y, 10).texture(0.5f + u, v).color(color);
                    builder.vertex(m, 0.5f + x, 0.5f + y, 10).texture(u, v).color(color);

                    previous = Math.min(nextPoint, checkpoint);
                    xPrev = x;
                    yPrev = y;
                }
            }
        }
    }

//    private void addPoint(Polygon polygon, double angle, double mradius) {
//        polygon.addPoint((int) (Math.sin(angle) * mradius) + radius, (int) (Math.cos(angle) * mradius) + radius);
//    }



    public interface IEntry {
        Color getColor();
        double getSample(Random random);
    }

    public record GaussianEntry(Color color, double mean, double stdDev) implements IEntry {
        @Override
        public Color getColor() {
            return color;
        }

        @Override
        public double getSample(Random random) {
            return random.nextGaussian() * stdDev + mean;
        }
    }

    public record Entry(Color color, Function<Random, Double> sampler) implements IEntry {
        @Override
        public Color getColor() {
            return color;
        }

        @Override
        public double getSample(Random random) {
            return sampler.apply(random);
        }
    }
}
