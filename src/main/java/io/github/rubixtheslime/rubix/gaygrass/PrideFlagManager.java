package io.github.rubixtheslime.rubix.gaygrass;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.LoaderContext;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.google.gson.JsonArray;
import io.github.rubixtheslime.rubix.RubixMod;
import net.minecraft.resource.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class PrideFlagManager extends SinglePreparationResourceReloader<ColorGetter> {
    public static final int BASE_LEVEL = 4;
    private static final ResourceFinder SVG_FINDER = new ResourceFinder("flags", ".svg");
    private final ResourceManager resourceManager;
    private ColorGetter colorGetter;

    public PrideFlagManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public static long merge(int x, int z) {
        return ((long) x & ~(-1L << 32)) | ((long) z << 32);
    }

    public int getColor(int initial, double x, double z) {
        return alphaBlend(colorGetter.getColor(x, z), initial);
    }

    private static int alphaBlend(int top, int bottom) {
        int res = 0;
        double alpha = (double) (top >>> 24) / 255d;
        for (int i = 0; i < 3; ++i) {
            int shift = i * 8;
            double a = (top >>> shift) & 255;
            double b = (bottom >>> shift) & 255;
            double c = Math.pow(Math.pow(a, 2.2) * alpha + Math.pow(b, 2.2) * (1 - alpha), 1d/2.2);
            res |= (int) c << shift;
        }
        return res;
    }

    @Override
    protected ColorGetter prepare(ResourceManager manager, Profiler profiler) {
        var flags = new HashMap<String, FlagEntry>();
        var modifiers = new HashMap<String, FlagEntry>();
        var globalEntry = new FlagEntry();

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
                        var flagEntry = FlagEntry.of(object, namespace);
                        if (flagEntry == null) continue;
                        var id = flagEntry.get(FlagEntry.ID);
                        if (id.equals(FlagEntry.GLOBAL_KEY)) {
                            globalEntry.merge(flagEntry);
                        } else if (flagEntry.getOr(FlagEntry.MODIFY, false)) {
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
            var flagEntry = FlagEntry.collect(globalEntry, entry.getValue(), mods);
            if (flagEntry.get(FlagEntry.WEIGHT) <= 0) continue;

            var id = Identifier.of(idStr);
            var resourceId = Identifier.of(flagEntry.get(FlagEntry.RESOURCE));
            var resourcePath = SVG_FINDER.toResourcePath(resourceId);
            var resource = manager.getResource(resourcePath);


            if (resource.isEmpty()) {
                RubixMod.LOGGER.error("flag not found: {}", resourceId);
                continue;
            }

            SVGDocument svgDocument;
            try {
                var stream = resource.get().getInputStream();
                var loader = new SVGLoader();
                svgDocument = loader.load(stream, null, LoaderContext.createDefault());
                stream.close();
            } catch (IOException e) {
                RubixMod.LOGGER.error("IO error happened: {}", e.getLocalizedMessage());
                return ColorGetter.ofEmpty();
            }
            if (svgDocument == null) {
                RubixMod.LOGGER.error("null svg document");
                return ColorGetter.ofEmpty();
            }

            var flagGetterBuilder = FlagGetter.Builder.of(svgDocument, flagEntry, id);
            if (flagGetterBuilder != null) builders.addLast(flagGetterBuilder);
        }
        return ColorGetter.of(builders, globalEntry);
    }

    @Override
    protected void apply(ColorGetter prepared, ResourceManager manager, Profiler profiler) {
        this.colorGetter = prepared;
        this.invalidateCaches();
    }

    public void setBiomeSeed(long seed) {
        this.colorGetter.setBiomeSeed(seed);
        this.invalidateCaches();
    }

    public void invalidateCaches() {
        this.colorGetter.invalidateCaches();
    }

}
