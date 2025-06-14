package io.github.rubixtheslime.rubix.redfile;

import net.minecraft.util.StringIdentifiable;

import java.util.function.Function;

public enum ExtendedRedfileTimeUnit implements StringIdentifiable {
    TICKS("ticks", 1, RedfileEndCondition::tickCondition, true),
    KTICKS("kticks", 1e3, RedfileEndCondition::tickCondition, true),
    MTICKS("Mticks", 1e6, RedfileEndCondition::tickCondition, true),

    SAMPLES("samples", 1, RedfileEndCondition::sampleCondition, true),
    kSAMPLES("ksamples", 1e3, RedfileEndCondition::sampleCondition, true),
    MSAMPLES("Msamples", 1e6, RedfileEndCondition::sampleCondition, true),
    GSAMPLES("Gsamples", 1e9, RedfileEndCondition::sampleCondition, true),

    TRIALS("trials", 1, RedfileEndCondition::trialCountCondition, false),
    kTRIALS("ktrials", 1e3, RedfileEndCondition::trialCountCondition, false),
    MTRIALS("Mtrials", 1e6, RedfileEndCondition::trialCountCondition, false),

    SECONDS("seconds", 1_000, RedfileEndCondition::timeCondition, true),
    MINUTES("minutes", 60_000, RedfileEndCondition::timeCondition, true),
    HOURS("hours", 3_600_000, RedfileEndCondition::timeCondition, true),
    ;

    private final String name;
    private final double scale;
    private final Function<Long, RedfileEndCondition.Builder> builderFunction;
    private final boolean isTrialLength;

    ExtendedRedfileTimeUnit(String name, double scale, Function<Long, RedfileEndCondition.Builder> builderFunction, boolean isTrialLength) {
        this.name = name;
        this.scale = scale;
        this.builderFunction = builderFunction;
        this.isTrialLength = isTrialLength;
    }

    @Override
    public String asString() {
        return name;
    }

    public RedfileEndCondition.Builder getEndCondition(double value) {
        return builderFunction.apply((long) (value * scale));
    }

    public boolean isTrialLength() {
        return isTrialLength;
    }
}
