package io.github.rubixtheslime.rubix.render;

import io.github.rubixtheslime.rubix.EnabledMods;
import io.github.rubixtheslime.rubix.RDebug;
import io.github.rubixtheslime.rubix.client.RubixModClient;
import io.github.rubixtheslime.rubix.util.MoreColor;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;
import net.minecraft.world.WorldView;
import org.lwjgl.opengl.GL32;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class DynColorBuilder implements VertexConsumer {

//    private static final int BLOCK_START_WIDTH = 3 + 2;
//    private static final int VERTEX_WIDTH = 4*3 + 3 + 2 + 1 + 1 + 4*2 + 2*2 + 4*3;
    private static final ThreadLocal<Boolean> greenScreening = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Boolean> postGreenScreening = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<BlockPos> currentBlockPos = ThreadLocal.withInitial(() -> null);
    private static final ThreadLocal<BlockState> currentBlockState = ThreadLocal.withInitial(() -> null);

    private final BlockColors blockColors;
    private final WorldView worldView;
    private final List<BuildingVertex> quadBuffer = new ArrayList<>(4);
    private final Map<Long, Map<Integer, List<BuiltColorVertex>>> deferred = new Long2ObjectOpenHashMap<>();
    private final VertexConsumer inner;
    private BuildingVertex tmpVertex = null;
    private static final Random random = new Xoroshiro128PlusPlusRandom(0);

    public DynColorBuilder(VertexConsumer inner) {
        this.inner = inner;
        var client = MinecraftClient.getInstance();
        blockColors = client.getBlockColors();
        worldView = client.world;
    }

    public static boolean isGreenScreening() {
        return EnabledMods.GAY_GRASS && greenScreening.get();
    }

    public static void setGreenScreening(boolean greenScreening) {
        DynColorBuilder.greenScreening.set(greenScreening);
    }

    public static void setBlock(BlockPos pos, BlockState state) {
        currentBlockPos.set(pos);
        currentBlockState.set(state);
    }

    public static boolean isPostGreenScreening() {
        return EnabledMods.GAY_GRASS && postGreenScreening.get();
    }

    private static void setPostGreenScreening(boolean postGreenScreening) {
        DynColorBuilder.postGreenScreening.set(postGreenScreening);
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        commit();
        tmpVertex = new BuildingVertex();
        tmpVertex.vertX = x;
        tmpVertex.vertY = y;
        tmpVertex.vertZ = z;
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        tmpVertex.red = red;
        tmpVertex.green = green;
        tmpVertex.blue = blue;
        tmpVertex.alpha = alpha;
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        tmpVertex.texU = u;
        tmpVertex.texV = v;
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        tmpVertex.lightU = u;
        tmpVertex.lightV = v;
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        tmpVertex.normX = x;
        tmpVertex.normY = y;
        tmpVertex.normZ = z;
        return this;
    }

    public BuiltData getColorData() {
        return deferred.isEmpty() ? null : new BuiltData(deferred);
    }

    public void endDirect() {
        // todo: put color data to ThreadLocal static so it can actually be retrieved
        commit();
    }

    private void commit() {
        if (tmpVertex == null) return;
        quadBuffer.addLast(tmpVertex);
        if (quadBuffer.size() != 4) return;

        var maxColorParts = quadBuffer.stream()
            .map(v -> new int[]{v.red, v.green, v.blue})
            .reduce((a, b) -> {
                for (int i = 0; i < a.length; i++) a[i] = Math.max(a[i], b[i]);
                return a;
            }).get();
        int maxRed = maxColorParts[0];
        int maxGreen = maxColorParts[1];
        int maxBlue = maxColorParts[2];

        setGreenScreening(false);
        if (maxRed == maxGreen && maxRed == maxBlue) {
            quadBuffer.forEach(v -> v.applyMost(inner).color(v.red, v.green, v.blue, v.alpha));
        } else {
            var pos = currentBlockPos.get();
            var manager = RubixModClient.prideFlagManager;
            boolean animated = manager.isAnimated(pos);
            setPostGreenScreening(!animated);
//            int color = -1;
            int color = maxGreen > maxBlue ? blockColors.getColor(currentBlockState.get(), worldView, pos, 1) : BiomeColors.getWaterColor(worldView, pos);
            setPostGreenScreening(false);
            if (RDebug.b1() ^ animated) {
                var list = deferred
                    .computeIfAbsent(pos.withY(0).asLong(), column -> new Int2ObjectOpenHashMap<>())
                    .computeIfAbsent(color, c -> new ArrayList<>());
                for (var v : quadBuffer) {
                    list.addLast(new BuiltColorVertex(
                        v.vertX, v.vertY, v.vertZ,
                        v.getShade(), (byte) v.alpha,
                        v.texU, v.texV,
                        (short) v.lightU, (short) v.lightV,
                        v.normX, v.normY, v.normZ
                    ));
                }
            } else {
                int red = ColorHelper.getRed(color);
                int green = ColorHelper.getRed(color);
                int blue = ColorHelper.getRed(color);
                quadBuffer.forEach(v -> {
                    int shade = v.getShade();
                    v.applyMost(inner).color(red * shade >> 23, green * shade >> 23, blue * shade >> 23, v.alpha);
                });
            }
        }
        setGreenScreening(true);
        quadBuffer.clear();

    }

    public static class BuiltData {
        private final Map<Long, Map<Integer, List<BuiltColorVertex>>> data;

        private BuiltData(Map<Long, Map<Integer, List<BuiltColorVertex>>> data) {
            // we only iterate through, so use array map
            this.data = new Long2ObjectArrayMap<>(data);
            this.data.replaceAll((key, inner) -> new Int2ObjectArrayMap<>(inner));
        }

        public void draw(VertexConsumer vertexConsumer, float originX, float originY, float originZ) {
            if (RDebug.b2()) return;
            var manager = RubixModClient.prideFlagManager;
            for (var columnEntry : data.entrySet()) {
                int colColor = RDebug.b3() ? (random.nextInt() & (-1 >>> 1)) : manager.getColor(BlockPos.fromLong(columnEntry.getKey()));
                var blender = MoreColor.QuickRgbBlender.of(colColor);
                for (var colorEntry : columnEntry.getValue().entrySet()) {
                    int[] baseColor = MoreColor.breakDownRgb(colorEntry.getKey());
                    blender.blend(baseColor);
                    for (var v : colorEntry.getValue()) {
                        vertexConsumer.vertex(v.vertX + originX, v.vertY + originY, v.vertZ + originZ)
                            .color(v.shade(baseColor[0]), v.shade(baseColor[1]), v.shade(baseColor[2]), v.alpha)
                            .texture(v.texU, v.texV)
                            .light(v.lightU, v.lightV)
                            .normal(v.normX, v.normY, v.normZ);
                    }
                }
            }
        }
    }

    private record BuiltColorVertex(float vertX, float vertY, float vertZ, int shadeMultiplier, byte alpha, float texU,
                                    float texV, short lightU, short lightV, float normX, float normY, float normZ) {
        public int shade(int part) {
            return part * shadeMultiplier >> 23;
        }
    }

    private static class BuildingVertex {
        private float vertX;
        private float vertY;
        private float vertZ;
        private int red;
        private int green;
        private int blue;
        private int alpha;
        private float texU;
        private float texV;
        private int lightU;
        private int lightV;
        private float normX;
        private float normY;
        private float normZ;

        private int getShade() {
            // precomputed inverse - must be ceiling div because reasons
            return ((Math.max(green, blue) << 23) - 1) / 255 + 1;
        }

        private VertexConsumer applyMost(VertexConsumer vertexConsumer) {
            return vertexConsumer.vertex(vertX, vertY, vertZ)
                .texture(texU, texV)
                .light(lightU, lightV)
                .normal(normX, normY, normZ);
        }
    }

}
