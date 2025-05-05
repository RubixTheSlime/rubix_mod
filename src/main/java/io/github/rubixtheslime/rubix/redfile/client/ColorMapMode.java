package io.github.rubixtheslime.rubix.redfile.client;

import net.minecraft.util.StringIdentifiable;

public enum ColorMapMode implements StringIdentifiable {
    RGB_GRADIENT("rgb_gradient"),
    MULTISHAPE("multishape");

    private final String name;

    private ColorMapMode(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return this.name;
    }
}
