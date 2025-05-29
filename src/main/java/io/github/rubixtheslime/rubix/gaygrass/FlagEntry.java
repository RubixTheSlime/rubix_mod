package io.github.rubixtheslime.rubix.gaygrass;

import com.google.gson.JsonObject;
import io.github.rubixtheslime.rubix.RubixMod;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

import java.util.HashMap;
import java.util.Map;


public final class FlagEntry {
    private static final Map<String, Key<?>> SIMPLE_KEYS = new HashMap<>();
    public static String GLOBAL_KEY = "*";
    public static Key<Double> WEIGHT = ofSimple(Double.class, "weight");
    public static Key<Double> DENSITY = ofSimple(Double.class, "density");
    public static Key<Double> MIN_SIZE = ofSimple(Double.class, "min_size");
    public static Key<Double> MAX_SIZE = ofSimple(Double.class, "max_size");
    public static Key<Double> OPACITY = ofSimple(Double.class, "opacity");
    public static Key<Double> ROTATION_DAMP = ofSimple(Double.class, "rotate_damp");
    public static Key<Long> PAPRIKA = ofSimple(Long.class, "paprika");
    public static Key<Boolean> ANTIALIAS = ofSimple(Boolean.class, "antialias");
    public static Key<Boolean> MODIFY = ofSimple(Boolean.class, "modify");
    public static Key<String> FORMAT = ofSimple(String.class, "format");
    public static Key<String> SCALE = ofSimple(String.class, "scale");
    public static Key<String> ID = ofSimple(String.class, "id");
    public static Key<String> RESOURCE = ofSimple(String.class, "resource");
//    public static Key<String> RESOURCE_NAMESPACE = new Key<>(String.class, "resource_namespace");

    private static <T> Key<T> ofSimple(Class<T> cls, String name) {
        var res = new Key<T>(cls, name);
        SIMPLE_KEYS.put(name, res);
        return res;
    }

    private final Map<Key<?>, Object> entries = new Reference2ObjectOpenHashMap<>();

    public static FlagEntry collect(FlagEntry global, FlagEntry entry, FlagEntry mods) {
        var res = new FlagEntry();
        res.entries.putAll(global.entries);
        res.entries.putAll(entry.entries);
        if (mods != null) res.entries.putAll(mods.entries);
        return res;
    }

    public static FlagEntry of(JsonObject object, String namespace) {
        var res = new FlagEntry();
        for (var entry : object.entrySet()) {
            if (!entry.getValue().isJsonPrimitive()) continue;
            var value = entry.getValue().getAsJsonPrimitive();
            var key = SIMPLE_KEYS.get(entry.getKey());
            if (key == null) continue;
            if (value.isNumber()) {
                if (Long.class.isAssignableFrom(key.cls)) res.entries.put(key, value.getAsLong());
                if (Double.class.isAssignableFrom(key.cls)) res.entries.put(key, value.getAsDouble());
            } else if (value.isBoolean()) {
                if (Boolean.class.isAssignableFrom(key.cls)) res.entries.put(key, value.getAsBoolean());
            } else if (value.isString()) {
                if (String.class.isAssignableFrom(key.cls)) res.entries.put(key, value.getAsString());
            }
        }
        if (!res.has(ID)) {
            return null;
        }
        var name = res.get(ID);
        if (name.equals(GLOBAL_KEY)) return res;
        try {
            if (!res.getOr(MODIFY, false)) {
                res.entries.put(ID, Identifier.of(namespace, name).toString());
                res.entries.put(RESOURCE, Identifier.of(namespace, res.getOr(RESOURCE, name)).toString());
            } else if (res.has(RESOURCE)) {
                res.entries.put(RESOURCE, Identifier.of(namespace, res.get(RESOURCE)).toString());
            }
        } catch (InvalidIdentifierException e) {
            RubixMod.LOGGER.error(e.getLocalizedMessage());
            return null;
        }
        return res;
    }

    public void merge(FlagEntry other) {
        this.entries.putAll(other.entries);
    }

    public boolean has(Key<?> key) {
        return entries.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Key<T> key) {
        return (T) entries.get(key);
    }

    public <T> T getOr(Key<T> key, T or) {
        return !has(key) ? or : get(key);
    }

    public record Key<T>(Class<T> cls, String name) {
    }

}
