package io.github.rubixtheslime.rubix.gaygrass;

import com.google.gson.JsonArray;
import io.github.rubixtheslime.rubix.EnabledMods;
import io.github.rubixtheslime.rubix.RubixMod;
import net.minecraft.resource.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;


public class PrideFlagManager extends SinglePreparationResourceReloader<ColorGetter> {
    private final ResourceManager resourceManager;
    private ColorGetter colorGetter;
    private long biomeSeed = 0;

    public void applyToAnimated(String idStr, Consumer<FlagBuffer.Animated> f) {
        colorGetter.applyToAnimated(idStr, f);
    }

    public void setTime(long value) {
        colorGetter.invalidateTileCache();
        colorGetter.applyToAnimated("*", animated -> animated.setTime(value));
    }

    public PrideFlagManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public static long merge(int x, int z) {
        return ((long) x & ~(-1L << 32)) | ((long) z << 32);
    }

    public int getColor(BlockPos blockPos, boolean animated) {
        if (blockPos == null || !animated && isAnimated(blockPos)) return 0;
        int x = blockPos.getX();
        int z = blockPos.getZ();
        return colorGetter.getColor(x, z);
    }

    public double getRotate(double x, double z, double damp) {
        return colorGetter.getRotate(x, z, damp);
    }

    @Override
    protected ColorGetter prepare(ResourceManager manager, Profiler profiler) {
        var flags = new HashMap<String, JsonFlagEntry>();
        var modifiers = new HashMap<String, JsonFlagEntry>();
        var globalEntry = new JsonFlagEntry();

        resourceManager.streamResourcePacks().forEach(pack -> {
            var namespaces = pack.getNamespaces(ResourceType.CLIENT_RESOURCES);
            for (String namespace : namespaces) {
                var resources = resourceManager.getAllResources(Identifier.of(namespace, "flags.json"));
                for (Resource resource : resources) {
                    if (!resource.getPack().getId().equals(pack.getId())) continue;
                    JsonArray array;
                    try {
                        Reader reader = resource.getReader();
                        array = JsonHelper.deserializeArray(reader);
                        reader.close();
                    } catch (Exception e) {
                        RubixMod.LOGGER.error(e.getLocalizedMessage());
                        continue;
                    }
                    for (var element : array) {
                        var object = element.getAsJsonObject();
                        var flagEntry = JsonFlagEntry.of(object, namespace);
                        if (flagEntry == null) continue;
                        var id = flagEntry.get(JsonFlagEntry.ID);
                        if (id.equals(JsonFlagEntry.GLOBAL_KEY)) {
                            globalEntry.merge(flagEntry);
                        } else if (flagEntry.getOr(JsonFlagEntry.MODIFY, false)) {
                            if (modifiers.containsKey(id)) {
                                modifiers.get(id).merge(flagEntry);
                            } else {
                                modifiers.put(id, flagEntry);
                            }
                        } else {
                            flags.put(id, flagEntry);
                        }
                    }
                }
            }
        });

        List<FlagGetter.Builder> builders = new ArrayList<>(flags.size());
        for (var entry : flags.entrySet()) {
            var idStr = entry.getKey();

            var mods = modifiers.get(idStr);
            var flagEntry = JsonFlagEntry.collect(globalEntry, entry.getValue(), mods);
            if (flagEntry.get(JsonFlagEntry.WEIGHT) <= 0) continue;

            var id = Identifier.of(idStr);
            var resourceId = Identifier.of(flagEntry.get(JsonFlagEntry.RESOURCE));
            try {
                var getter = FlagBuffer.Getter.of(flagEntry.get(JsonFlagEntry.FORMAT));

                var resourcePath = getter.toResourcePath(resourceId);
                var resource = manager.getResource(resourcePath);
                if (resource.isEmpty()) throw new RuntimeException("failed to find resource");

                var flagBuffer = getter.build(resource.get(), flagEntry);
                if (flagBuffer == null) throw new RuntimeException("null buffer object");
                var flagData = FlagData.of(flagEntry, flagBuffer, id);

                var flagGetterBuilder = FlagGetter.Builder.of(flagData, flagEntry);
                builders.addLast(flagGetterBuilder);
            } catch (RuntimeException e) {
                RubixMod.LOGGER.error("an error occurred while preparing {} (resource id {}): {}", id, resourceId, e.getLocalizedMessage());
            }
        }
        return ColorGetter.of(builders, globalEntry);
    }

    @Override
    protected void apply(ColorGetter prepared, ResourceManager manager, Profiler profiler) {
        prepared.setBiomeSeed(biomeSeed);
        this.colorGetter = prepared;
    }

    public void setBiomeSeed(long seed) {
        biomeSeed = seed;
        this.colorGetter.setBiomeSeed(seed);
        this.invalidateCaches();
    }

    public void invalidateCaches() {
        this.colorGetter.invalidateCaches();
    }

    public boolean isAnimated(BlockPos pos) {
//        return EnabledMods.GAY_GRASS_VIDEO && colorGetter.getColor(pos.getX(), pos.getZ()) != 0;
        return EnabledMods.GAY_GRASS_VIDEO && colorGetter.isAnimated(pos.getX(), pos.getZ());
    }

    public Stream<Identifier> getAnimatedNames() {
        return colorGetter.getAnimatedNames();
    }
}
