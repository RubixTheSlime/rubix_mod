package io.github.rubixtheslime.rubix.redfile;

import net.minecraft.util.StringIdentifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public enum ExtendedRedfileTimeUnit implements StringIdentifiable {
    TICKS("ticks", 1, RedfileEndCondition::tickCondition, true, true),
    KTICKS("kticks", 1e3, RedfileEndCondition::tickCondition, true, true),
    MTICKS("Mticks", 1e6, RedfileEndCondition::tickCondition, true, true),

    SAMPLES("samples", 1, RedfileEndCondition::sampleCondition, true, false),
    kSAMPLES("ksamples", 1e3, RedfileEndCondition::sampleCondition, true, false),
    MSAMPLES("Msamples", 1e6, RedfileEndCondition::sampleCondition, true, false),
    GSAMPLES("Gsamples", 1e9, RedfileEndCondition::sampleCondition, true, false),

    TRIALS("trials", 1, RedfileEndCondition::trialCountCondition, false, true),
    kTRIALS("ktrials", 1e3, RedfileEndCondition::trialCountCondition, false, true),
    MTRIALS("Mtrials", 1e6, RedfileEndCondition::trialCountCondition, false, true),
    GTRIALS("Gtrials", 1e9, RedfileEndCondition::trialCountCondition, false, true),

    SECONDS("seconds", 1_000, RedfileEndCondition::timeCondition, true, true),
    MINUTES("minutes", 60_000, RedfileEndCondition::timeCondition, true, true),
    HOURS("hours", 3_600_000, RedfileEndCondition::timeCondition, true, true),
    ;

    private static final ExtendedRedfileTimeUnit[] TRIAL_LENGTH_UNITS;
    private static final ExtendedRedfileTimeUnit[] RUN_LENGTH_UNITS;

    static {
        List<ExtendedRedfileTimeUnit> trialList = new ArrayList<>();
        List<ExtendedRedfileTimeUnit> runList = new ArrayList<>();
        for (var value : values()) {
            if (value.isTrialLength) trialList.addLast(value);
            if (value.isRunLength) runList.addLast(value);
        }
        TRIAL_LENGTH_UNITS = trialList.toArray(new ExtendedRedfileTimeUnit[0]);
        RUN_LENGTH_UNITS = runList.toArray(new ExtendedRedfileTimeUnit[0]);
    }

    private final String name;
    private final double scale;
    private final Function<Long, RedfileEndCondition.Builder> builderFunction;
    private final boolean isTrialLength;
    private final boolean isRunLength;

    ExtendedRedfileTimeUnit(String name, double scale, Function<Long, RedfileEndCondition.Builder> builderFunction, boolean isTrialLength, boolean isRunLength) {
        this.name = name;
        this.scale = scale;
        this.builderFunction = builderFunction;
        this.isTrialLength = isTrialLength;
        this.isRunLength = isRunLength;
    }

    @Override
    public String asString() {
        return name;
    }

    public RedfileEndCondition.Builder getEndCondition(double value) {
        return builderFunction.apply((long) (value * scale));
    }

    public static ExtendedRedfileTimeUnit[] trialLengths() {
        return TRIAL_LENGTH_UNITS;
    }

    public static ExtendedRedfileTimeUnit[] runLengths() {
        return RUN_LENGTH_UNITS;
    }
}
