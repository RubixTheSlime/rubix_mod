package io.github.rubixtheslime.rubix.redfile.client;

import io.github.rubixtheslime.rubix.RubixMod;
import io.github.rubixtheslime.rubix.render.ModRenderLayer;
import io.github.rubixtheslime.rubix.render.XrayRenderLayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.joml.Vector3i;

import java.util.Arrays;

@Environment(EnvType.CLIENT)
public interface ColorMap {
    Identifier REDFILE_MULTISHAPE_TILES = Identifier.of(RubixMod.MOD_ID, "textures/misc/redfile_multishape_tiles.png");


    void addVerts(BufferBuilder tileBuilder, MatrixStack stack, Vector3i pos, int value);
    XrayRenderLayer getRenderLayer();
    int getColor(float logNanos);

    abstract class Interpolator<T, U> implements ColorMap {
        private final T[] points;
        private final int offset;

        protected Interpolator(U[] points, T[] ref, int offset) {
            this.points = Arrays.stream(points).map(this::prepare).toList().toArray(ref);
            this.offset = offset;
        }

        @Override
        public int getColor(float logNanos) {
            float x = Math.clamp(logNanos - offset, 0, points.length - 1);
            int index = (int) Math.floor(x);
            T value = (index == points.length - 1)
                ? points[points.length - 1]
                : interpolate(points[index], points[index + 1], x % 1f);
            return convert(value);
        }

        abstract T prepare(U input);

        abstract T interpolate(T x1, T x2, float progress);

        abstract int convert(T x);
    }

    abstract class DoubleArrayInterpolator extends Interpolator<double[], double[]> {
        public DoubleArrayInterpolator(int offset, double[][] points) {
            super(points, new double[0][], offset);
        }

        @Override
        double[] prepare(double[] input) {
            return input;
        }

        @Override
        double[] interpolate(double[] x1, double[] x2, float progress) {
            var res = Arrays.copyOf(x1, x1.length);
            for (int i = 0; i < res.length; ++i) {
                res[i] = x1[i] * (1 - progress) + x2[i] * progress;
            }
            return res;
        }
    }

    class RGBInterpolate extends DoubleArrayInterpolator {
        public RGBInterpolate(int offset, int[] points) {
            super(offset, Arrays.stream(points).mapToObj(x -> {
                var res = new double[3];
                for (int i = 0; i < 3; ++i) {
                    res[i] = Math.pow((x >> (i * 8)) & 0xff, 2);
                }
                return res;
            }).toList().toArray(new double[0][]));
        }

        @Override
        int convert(double[] x) {
            int res = 0;
            for (int i = 2; i >= 0; --i) {
                res = (res << 8) | (int) Math.clamp(Math.pow(x[i], 1d/2), 0, 255);
            }
            return res;
        }

        @Override
        public void addVerts(BufferBuilder tileBuilder, MatrixStack stack, Vector3i pos, int value) {
            var r = ColorHelper.getRed(value);
            var g = ColorHelper.getGreen(value);
            var b = ColorHelper.getBlue(value);
            float x1 = pos.x;
            float x2 = pos.x + 1;
            float y = pos.y + 0.002f;
            float z1 = pos.z;
            float z2 = pos.z + 1;
            var m = stack.peek();
            tileBuilder.vertex(m, x1, y, z1).color(r, g, b, 255);
            tileBuilder.vertex(m, x2, y, z1).color(r, g, b, 255);
            tileBuilder.vertex(m, x2, y, z2).color(r, g, b, 255);
            tileBuilder.vertex(m, x1, y, z2).color(r, g, b, 255);
        }

        @Override
        public XrayRenderLayer getRenderLayer() {
            return ModRenderLayer.HIGHLIGHT;
        }
    }

    class MultiShapeColorMap implements ColorMap {
        private final int[] colors;
        public MultiShapeColorMap(int[] colors) {
            this.colors = colors;
        }

        @Override
        public void addVerts(BufferBuilder tileBuilder, MatrixStack stack, Vector3i pos, int value) {
            int stageIndex = (value >> 7) & 3;
            int interpolateIndex = (value >> 4) & 7;
            int colorIndex = (value >> 1) & 7;
            int color1 = colors[colorIndex];
            int color2 = colors[Math.min(colorIndex + 1, 4)];

            var r1 =  ColorHelper.getRed(color1);
            var g1 =  ColorHelper.getGreen(color1);
            var b1 =  ColorHelper.getBlue(color1);
            var r2 =  ColorHelper.getRed(color2);
            var g2 =  ColorHelper.getGreen(color2);
            var b2 =  ColorHelper.getBlue(color2);

            float x1 = pos.x;
            float x2 = pos.x + 1;
            float y = pos.y + 0.002f;
            float z1 = pos.z;
            float z2 = pos.z + 1;

            float u1 = (float) interpolateIndex * (1f/8);
            float u2 = u1 + 0.125f;
            float v1 = (float) stageIndex * (1f/3);
            float v2 = v1 + 1f/6;
            float v3 = v1 + 2f/6;

            var m = stack.peek();
            tileBuilder.vertex(m, x1, y, z1).texture(u1, v1).color(r1, g1, b1, 255);
            tileBuilder.vertex(m, x2, y, z1).texture(u2, v1).color(r1, g1, b1, 255);
            tileBuilder.vertex(m, x2, y, z2).texture(u2, v2).color(r1, g1, b1, 255);
            tileBuilder.vertex(m, x1, y, z2).texture(u1, v2).color(r1, g1, b1, 255);

            tileBuilder.vertex(m, x1, y, z1).texture(u1, v2).color(r2, g2, b2, 255);
            tileBuilder.vertex(m, x2, y, z1).texture(u2, v2).color(r2, g2, b2, 255);
            tileBuilder.vertex(m, x2, y, z2).texture(u2, v3).color(r2, g2, b2, 255);
            tileBuilder.vertex(m, x1, y, z2).texture(u1, v3).color(r2, g2, b2, 255);
        }

        @Override
        public XrayRenderLayer getRenderLayer() {
            return ModRenderLayer.HIGHLIGHT_REDFILE_MULTISHAPE_TILE;
        }

        @Override
        public int getColor(float logNanos) {
            int index = Math.round(Math.clamp(logNanos + 3, 0, 12) * 8);
            int textureIndex = index % 24;
            int interpolate = textureIndex % 8;
            int stage = textureIndex / 8;
            int colorIndex = index / 24;
            return stage << 7 | interpolate << 4 | colorIndex << 1 | 1;
        }

    }

}
