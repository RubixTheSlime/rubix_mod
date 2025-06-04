package io.github.rubixtheslime.rubix.redfile;

import io.github.rubixtheslime.rubix.ModRegistries;
import net.minecraft.util.Identifier;

public class RedfileTag {

    public static RedfileTag of() {
        return new RedfileTag();
    }

    public Identifier id() {
        return ModRegistries.REDFILE_TAG.getId(this);
    }

    @Override
    public String toString() {
        return id().toTranslationKey();
    }
}
