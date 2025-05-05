package io.github.rubixtheslime.rubix.redfile;

import net.minecraft.util.StringIdentifiable;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum ExtendedRedfileTimeUnit implements StringIdentifiable {
    TICKS("ticks", 1, RedfileTimeUnit.TICKS),
    KTICKS("kticks", 1e3, RedfileTimeUnit.TICKS),
    MTICKS("Mticks", 1e6, RedfileTimeUnit.TICKS),

    SAMPLES("samples", 1, RedfileTimeUnit.SAMPLES),
    kSAMPLES("ksamples", 1e3, RedfileTimeUnit.SAMPLES),
    MSAMPLES("Msamples", 1e6, RedfileTimeUnit.SAMPLES),
    GSAMPLES("Gsamples", 1e9, RedfileTimeUnit.SAMPLES),

    SECONDS("seconds", 1, RedfileTimeUnit.SECONDS),
    MINUTES("minutes", 60, RedfileTimeUnit.SECONDS),
    HOURS("hours", 3600, RedfileTimeUnit.SECONDS),

    INDEFINITE("nolimit", 1, RedfileTimeUnit.INDEFINITE),
    ;


    private final String name;
    private final double scale;
    private final RedfileTimeUnit unit;

    ExtendedRedfileTimeUnit(String name, double scale, RedfileTimeUnit unit) {
        this.name = name;
        this.scale = scale;
        this.unit = unit;
    }

    @Override
    public String asString() {
        return name;
    }

    public double getScale() {
        return scale;
    }

    public RedfileTimeUnit getUnit() {
        return unit;
    }
}
