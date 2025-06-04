package io.github.rubixtheslime.rubix;

import com.mojang.serialization.Lifecycle;
import io.github.rubixtheslime.rubix.redfile.RedfileTag;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;

public class ModRegistries {
    public static final Registry<RedfileTag> REDFILE_TAG = register("redfile_tag");

    private static <T> Registry<T> register(String name) {
        RegistryKey<Registry<T>> key = RegistryKey.ofRegistry(Identifier.of(RubixMod.MOD_ID, name));
        return new SimpleRegistry<>(key, Lifecycle.stable());
    }


}
