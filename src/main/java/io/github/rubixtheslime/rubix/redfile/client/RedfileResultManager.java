package io.github.rubixtheslime.rubix.redfile.client;


import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.rubixtheslime.rubix.ModRegistries;
import io.github.rubixtheslime.rubix.RubixConfig;
import io.github.rubixtheslime.rubix.RubixMod;
import io.github.rubixtheslime.rubix.network.RedfileResultPacket;
import io.github.rubixtheslime.rubix.network.RedfileTranslationPacket;
import io.github.rubixtheslime.rubix.redfile.RedfileTag;
import io.github.rubixtheslime.rubix.redfile.RedfileTags;
import io.github.rubixtheslime.rubix.redfile.TaggedStats;
import io.github.rubixtheslime.rubix.render.ModRenderLayer;
import io.github.rubixtheslime.rubix.util.MoreMath;
import io.github.rubixtheslime.rubix.util.SetOperation;
import io.github.rubixtheslime.rubix.util.Util;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.*;

import java.awt.*;
import java.lang.Math;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class RedfileResultManager {
    private static final BufferAllocator LINE_BUFFER_ALLOCATOR = new BufferAllocator(1536);
    private static final RubixConfig.RedfileOptions CONFIG = RubixMod.CONFIG.redfileOptions;
    private final Map<String, WorldEntry> results = new HashMap<>();
    private final List<RedfileTag> translation = new ArrayList<>();
    private Set<RedfileTag> activeTags;
    private ColorMap colorMap = null;
    private RenderTrie tileRenderTrie = null;
    private RenderTrie lineRenderTrie = null;
    private World worldRenderedAt = null;
    private TaggedStats.Display sumOfSelected = null;

    public void addDebug(BlockPos pos, int width, World world) {
        Map<Long, int[]> map = new Long2ObjectOpenHashMap<>(width * 12);
        for (int i = 0; i < 12 * width; i++) {
            var mv = new MoreMath.MeanAndVar(Math.pow(10, ((double) i / width) - 9), 0);
            int[] entry = TaggedStats.of(Map.of(RedfileTags.UNTAGGED, mv)).pack();
            int dcol = i % (width * 2);
            long longBlockPos = BlockPos.asLong(pos.getX() + Math.min(dcol, width * 2  - 1 - dcol), pos.getY(), pos.getZ() + (i / width));
            map.put(longBlockPos, entry);
        }
        addResultDirect(new RedfileResultPacket(map, false, world.getRegistryKey().getValue()));
    }

    public boolean addEmpty(World world) {
        addResultDirect(new RedfileResultPacket(Collections.emptyMap(), false, world.getRegistryKey().getValue()));
        return true;
    }

    public void addResult(RedfileResultPacket result) {
        var data = new Long2ObjectOpenHashMap<>(result.data());
        data.replaceAll((posLong, packed) -> TaggedStats.unpack(packed, false, translation).pack());
        addResultDirect(new RedfileResultPacket(data, result.splitTags(), result.worldIdentifier()));
    }

    public void addResultDirect(RedfileResultPacket result) {
//        var client = MinecraftClient.getInstance();
        var entry = results.computeIfAbsent(result.worldIdentifier().toString(), x ->
            new WorldEntry(new ArrayList<>(), new LongOpenHashSet())
        );
        entry.results.addLast(new RedfileResult(result.data(), this));
        tileRenderTrie = null;
    }

    private WorldEntry get(World world) {
        return results.get(world.getRegistryKey().getValue().toString());
    }

    private WorldEntry remove(World world) {
        return results.remove(world.getRegistryKey().getValue().toString());
    }

    public Set<RedfileTag> getActiveTags() {
        if (activeTags == null) {
            activeTags = new ReferenceOpenHashSet<>(ModRegistries.REDFILE_TAG.size());
            setTagActive(null, true);
        }
        return activeTags;
    }

    public void render(MatrixStack stack, Vec3d cameraPos, Frustum frustum, World world) {
        var worldEntry = get(world);
        if (worldEntry == null || worldEntry.results.isEmpty()) return;

        if (world != worldRenderedAt) {
            worldRenderedAt = world;
            tileRenderTrie = null;
            lineRenderTrie = null;
        }

        ensureColorMap();
        ensureTileRenderTrie(worldEntry);

        Vector3d offset = new Vector3d(cameraPos.x, cameraPos.y, cameraPos.z);
        Vector3i camInt = new Vector3i(offset, RoundingMode.FLOOR);
        offset.sub(new Vector3d(camInt));
        stack.push();
        stack.translate(-offset.x, -offset.y, -offset.z);

        renderTiles(stack, camInt, frustum);

        if (!worldEntry.selected.isEmpty()) {
            ensureLineRenderTrie(worldEntry);
            renderLines(stack, camInt, frustum);
        }

        stack.pop();
    }

    private void renderTiles(MatrixStack stack, Vector3i camInt, Frustum frustum) {
        float opacity = CONFIG.opacity();
        float xray = CONFIG.xrayEnabled() ? CONFIG.xray() : 0;

        boolean[] anyFound = {false};

        var renderLayer = colorMap.getRenderLayer();
        var tileBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, renderLayer.getVertexFormat());
        tileRenderTrie.visit(camInt, frustum, (pos, value) -> {
            if (value == 0) return;
            colorMap.addVerts(tileBuilder, stack, pos, value);
            anyFound[0] = true;
        });

        if (!anyFound[0]) return;

        var buffer = tileBuilder.end();
        renderLayer.setAlphas(opacity, opacity * xray);
        renderLayer.draw(buffer);
    }

    private void renderLines(MatrixStack stack, Vector3i camInt, Frustum frustum) {
        boolean[] anyFound = {false};

        Color color;
        try {
            color = Color.decode("#" + RubixMod.CONFIG.redfileOptions.selectionColor());
        } catch (NumberFormatException ignored) {
            color = Color.GRAY;
        }
        int r = color.getRed();
        int g = color.getRed();
        int b = color.getRed();
        var m = stack.peek();
        var lineBuilder = new BufferBuilder(LINE_BUFFER_ALLOCATOR, VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR_NORMAL);
        lineRenderTrie.visit(camInt, frustum, (pos, code) -> {
            if ((code & 1) != 0) {
                lineBuilder.vertex(m, pos.x, pos.y + 0.01f, pos.z).color(r, g, b, 255).normal(m, 1, 0, 0);
                lineBuilder.vertex(m, pos.x + 1, pos.y + 0.01f, pos.z).color(r, g, b, 255).normal(m, 1, 0, 0);
                anyFound[0] = true;
            }
            if ((code & 2) != 0) {
                lineBuilder.vertex(m, pos.x, pos.y + 0.01f, pos.z).color(r, g, b, 255).normal(m, 0, 0, 1);
                lineBuilder.vertex(m, pos.x, pos.y + 0.01f, pos.z + 1).color(r, g, b, 255).normal(m, 0, 0, 1);
                anyFound[0] = true;
            }
        });
        if (!anyFound[0]) return;
        var buffer = lineBuilder.end();
        ModRenderLayer.HIGHLIGHT_LINE.draw(buffer);
    }

    public TaggedStats.Display getSumOfSelected(World world) {
        if (sumOfSelected == null) {
            var worldEntry = get(world);
            if (worldEntry == null || worldEntry.solo == null) return null;
            var soloed = worldEntry.solo;
            var summer = new TaggedStats.Accumulator();
            worldEntry.selected.stream()
                .filter(worldEntry::checkLayer)
                .forEach(key -> summer.add(soloed.get(key).stats().stats()));
//            sumOfSelected = MoreMath.clampZero(summer.finish().middleInterval(0.05));
            sumOfSelected = summer.finish().getDisplay(getActiveTags());
        }
        return sumOfSelected;
    }

    public void toggleLookingAt(MinecraftClient client) {
        var tile = getLookingAt(client);
        if (tile == null) return;
        assert client.world != null;
        var worldEntry = get(client.world);
        if (worldEntry == null) return;
        boolean wasEmpty = worldEntry.selected.isEmpty();
        long blockPosLong = tile.pos.asLong();
        if (!worldEntry.selected.remove(blockPosLong)) worldEntry.selected.add(blockPosLong);
        if (wasEmpty != worldEntry.selected.isEmpty() && worldEntry.results.size() > 1) tileRenderTrie = null;
        worldEntry.solo = worldEntry.selected.isEmpty() ? null : tile.master;
        lineRenderTrie = null;
        sumOfSelected = null;
    }

    public ResultTile getLookingAt(MinecraftClient client) {
        if (tileRenderTrie == null || client.world == null) return null;
        var camera = client.gameRenderer.getCamera();
        var pos4i = tileRenderTrie.streamRayCast(Util.vec3dToVector3d(camera.getPos()), new Vector3d(camera.getHorizontalPlane()))
            .filter(entry -> entry.w != 0)
            .findFirst()
            .orElse(null);
        if (pos4i == null) return null;
        long blockPosLong = BlockPos.asLong(pos4i.x, pos4i.y, pos4i.z);
        var worldEntry = get(client.world);
        return worldEntry == null ? null : worldEntry.streamResults()
            .map(r -> {
                var tile = r.get(blockPosLong);
                return Map.entry(tile, tile.getValue());
            })
            .filter(entry -> entry.getValue() != 0)
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    public boolean clearResults(World world) {
        var entry = remove(world);
        boolean res = entry != null;
        if (res) {
            tileRenderTrie = null;
            lineRenderTrie = null;
            sumOfSelected = null;
        }
        return res;
    }

    public void markColorDirty() {
        colorMap = null;
        tileRenderTrie = null;
    }

    private void ensureTileRenderTrie(WorldEntry worldEntry) {
        if (tileRenderTrie != null) return;
        Map<Long, Float> amounts = new Long2FloatOpenHashMap();
        worldEntry.streamResults()
            .filter(RedfileResult::isActive)
            .forEach(display -> display.stream().forEach(tile -> {
                if (!worldEntry.checkLayer(tile.pos.asLong())) return;
                double value = tile.getValue();
                if (value == 0) return;
                float logNanos = (float) Math.log(value) * (1f / (float) Math.log(10)) + 6f;
                amounts.merge(tile.pos.asLong(), logNanos, Math::max);
            }));
        Map<Long, Integer> colors = new Long2IntOpenHashMap();
        amounts.forEach((pos, logNanos) -> colors.put(pos, colorMap.getColor(logNanos) | Integer.MIN_VALUE));
        tileRenderTrie = new RenderTrie(colors);
    }

    private void ensureLineRenderTrie(WorldEntry worldEntry) {
        if (lineRenderTrie != null) return;
        Map<Long, Integer> codes = new Long2IntOpenHashMap();
        worldEntry.selected.forEach(blockPosLong -> {
            if (!worldEntry.checkLayer(blockPosLong)) return;
            codes.merge(blockPosLong, 3, (a, b) -> a ^ b);
            codes.merge(BlockPos.offset(blockPosLong, Direction.SOUTH), 1, (a, b) -> a ^ b);
            codes.merge(BlockPos.offset(blockPosLong, Direction.EAST), 2, (a, b) -> a ^ b);
        });
        lineRenderTrie = new RenderTrie(codes);
    }

    @SuppressWarnings("unchecked")
    private static final Supplier<String>[] GRADIENT_GETTERS = new Supplier[] {
        CONFIG::gradientP1Color,
        CONFIG::gradientP10Color,
        CONFIG::gradientP100Color,
        CONFIG::gradientN1Color,
        CONFIG::gradientN10Color,
        CONFIG::gradientN100Color,
        CONFIG::gradientU1Color,
        CONFIG::gradientU10Color,
        CONFIG::gradientU100Color,
        CONFIG::gradientM1Color,
        CONFIG::gradientM10Color,
        CONFIG::gradientM100Color,
        CONFIG::gradientS1Color
    };

    @SuppressWarnings("unchecked")
    private static final Supplier<String>[] MULTISHAPE_GETTERS = new Supplier[]{
        CONFIG::multishapePColor,
        CONFIG::multishapeNColor,
        CONFIG::multishapeUColor,
        CONFIG::multishapeMColor,
        CONFIG::multishapeSColor
    };

    private static int[] getColors(Supplier<String>[] getters) {
        var colors = new int[getters.length];
        for (int i = 0; i < getters.length; i++) {
            Color c = Color.GRAY;
            try {
                c = Color.decode("#" + getters[i].get());
            } catch (NumberFormatException ignored) {
            }
            colors[i] = c.getRGB();
        }
        return colors;
    }

    private void ensureColorMap() {
        if (colorMap != null) return;

        var o = RubixMod.CONFIG.redfileOptions;
        colorMap = switch (o.colorMapMode()) {
            case RGB_GRADIENT -> new ColorMap.RGBInterpolate(-3, getColors(GRADIENT_GETTERS));
            case MULTISHAPE -> new ColorMap.MultiShapeColorMap(getColors(MULTISHAPE_GETTERS));
        };


    }

    public boolean drop(World world, int index) {
        var worldEntry = get(world);
        if (worldEntry == null) return false;
        var entry = worldEntry.remove(index);
        if (entry == null) return false;
        tileRenderTrie = null;
        if (entry == worldEntry.solo) {
            worldEntry.clearSelection(this);
        }
        return true;
    }

    public boolean setLayer(World world, Integer layer) {
        var worldEntry = get(world);
        if (worldEntry == null || Objects.equals(worldEntry.layer, layer)) return false;
        worldEntry.layer = layer;
        tileRenderTrie = null;
        lineRenderTrie = null;
        sumOfSelected = null;
        return true;
    }

    public boolean moveLayer(World world, boolean up) {
        var worldEntry = get(world);
        if (worldEntry == null || worldEntry.layer == null) return false;
        worldEntry.layer += up ? 1 : -1;
        tileRenderTrie = null;
        lineRenderTrie = null;
        sumOfSelected = null;
        return true;
    }

    public boolean setAllActive(World world, boolean active) {
        var worldEntry = get(world);
        if (worldEntry == null) return false;
        var res = worldEntry.results.stream().anyMatch(x -> x.active != active);
        if (!res) return false;
        worldEntry.results.forEach(x -> x.setActive(active));
        if (worldEntry.solo != null && !active) {
            worldEntry.clearSelection(this);
        }
        tileRenderTrie = null;
        return true;
    }

    public boolean setActive(World world, int index, boolean active) {
        var worldEntry = get(world);
        if (worldEntry == null) return false;
        var entry = worldEntry.get(index);
        if (entry == null) return false;
        if (entry == worldEntry.solo) {
            worldEntry.clearSelection(this);
        }
        var res = entry.active != active;
        if (res) tileRenderTrie = null;
        entry.active = active;
        return res;
    }

    public boolean selectAll(World world, int index) {
        setActive(world, index, true);
        var worldEntry = get(world);
        if (worldEntry == null) return false;
        var entry = worldEntry.get(index);
        if (entry == null) return false;
        worldEntry.solo = entry;
        worldEntry.selected.clear();
        return applySetOperationToSelection(world, SetOperation.ASSIGN, null, null);
    }

    public boolean isSelecting(World world) {
        var entry = get(world);
        return entry != null && entry.solo != null;
    }

    public boolean applySetOperationToSelection(World world, SetOperation operation, BlockBox box, BlockStateArgument blockStateArgument) {
        var entry = get(world);
        if (entry == null) return false;
        Set<Long> described = new LongOpenHashSet();
        entry.solo.data.keySet().forEach(blockPosLong ->{
            var pos = BlockPos.fromLong(blockPosLong);
            if (box != null && !box.contains(pos)) return;
            if (blockStateArgument != null) {
                try {
                    if (!blockStateArgument.test(new CachedBlockPosition(world, pos, true))) return;
                } catch (NullPointerException ignored) {
                    return;
                }
            }
            described.add(blockPosLong);
        });
        boolean wasEmpty = entry.selected.isEmpty();
        operation.applyToSet(entry.selected, described);
        if (entry.selected.isEmpty()) entry.solo = null;
        if (entry.selected.isEmpty() != wasEmpty) tileRenderTrie = null;
        lineRenderTrie = null;
        sumOfSelected = null;
        return true;
    }

    public void setTagActive(RedfileTag tag, boolean enable) {
        if (tag == null) {
            if (enable) {
                ModRegistries.REDFILE_TAG.stream().forEach(activeTags::add);
            } else {
                activeTags.clear();
            }
        } else {
            if (enable) {
                activeTags.add(tag);
            } else {
                activeTags.remove(tag);
            }
        }

        tileRenderTrie = null;
        lineRenderTrie = null;
        sumOfSelected = null;
    }

    public void setTranslation(RedfileTranslationPacket packet) {
        translation.clear();
        for (var name : packet.names()) {
            var tag = ModRegistries.REDFILE_TAG.get(Identifier.of(name));
            translation.addLast(tag == null ? RedfileTags.UNFAMILIAR : tag);
        }
    }

    private static final class WorldEntry {
        private final List<RedfileResult> results;
        private final Set<Long> selected;
        private Integer layer = null;
        private RedfileResult solo = null;

        private WorldEntry(List<RedfileResult> results, Set<Long> selected) {
            this.results = results;
            this.selected = selected;
        }

        public RedfileResult remove(int i) {
            if (i < 0) i += results.size();
            if (i < 0 || i >= results.size()) return null;
            return results.remove(i);
        }

        public RedfileResult get(int i) {
            if (i < 0) i += results.size();
            if (i < 0 || i >= results.size()) return null;
            return results.get(i);
        }

        public void clearSelection(RedfileResultManager manager) {
            this.selected.clear();
            this.solo = null;
            manager.sumOfSelected = null;
            manager.lineRenderTrie = null;
        }

        public boolean checkLayer(long blockPosLong) {
            return layer == null || layer.equals(BlockPos.unpackLongY(blockPosLong));
        }

        public Stream<RedfileResult> streamResults() {
            return (solo == null) ? results.stream().filter(RedfileResult::isActive) : Stream.of(solo);
        }

        public List<RedfileResult> results() {
            return results;
        }

        public Set<Long> selected() {
            return selected;
        }
    }

    public static final class RedfileResult {
        private final Map<Long, int[]> data;
        private final RedfileResultManager manager;
        private boolean active = true;

        private RedfileResult(Map<Long, int[]> data, RedfileResultManager manager) {
            this.data = data;
            this.manager = manager;
        }

        public ResultTile get(long blockPosLong) {
            var statsEntry = TaggedStats.unpack(data.get(blockPosLong), false);
            return new ResultTile(BlockPos.fromLong(blockPosLong), statsEntry.getDisplay(manager.getActiveTags()), this);
        }

        public Stream<ResultTile> stream() {
            return data.entrySet()
                .stream()
                .map(entry -> new ResultTile(
                    BlockPos.fromLong(entry.getKey()),
                    TaggedStats.unpack(entry.getValue(), false).getDisplay(manager.getActiveTags()),
                    this
                ));
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

    }

    public record ResultTile(BlockPos pos, TaggedStats.Display stats, RedfileResult master) {
//        public ConfidenceInterval getInterval() {
//            return MoreMath.clampZero(meanAndVar.middleInterval(RubixMod.CONFIG.redfileOptions.alpha()));
//        }

        public boolean isEmpty() {
            return stats.isEmpty();
        }

        public double getValue() {
            var mv = stats.sum();
            return mv.mean();
        }

    }

}
