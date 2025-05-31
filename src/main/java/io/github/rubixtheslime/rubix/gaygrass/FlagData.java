package io.github.rubixtheslime.rubix.gaygrass;

import net.minecraft.util.Identifier;

import java.awt.*;

public class FlagData {
    public final FlagBuffer buffer;
    public final Identifier identifier;
    public final float randomColorAlpha;
    public final float opacity;
    public final double rotationDamp;
    public final Scale scale;
    public final Object antialiasKey;

    private FlagData(JsonFlagEntry flagEntry, FlagBuffer buffer, Identifier identifier, Scale scale) {
        this.buffer = buffer;
        this.identifier = identifier;
        this.randomColorAlpha = (float) (double) flagEntry.get(JsonFlagEntry.RANDOM_COLOR_ALPHA);
        this.opacity = (float) (double) flagEntry.get(JsonFlagEntry.OPACITY);
        this.antialiasKey = flagEntry.get(JsonFlagEntry.ANTIALIAS) ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF;
        this.scale = scale;
        this.rotationDamp = flagEntry.get(JsonFlagEntry.ROTATION_DAMP);
    }

    public static FlagData of(JsonFlagEntry flagEntry, FlagBuffer buffer, Identifier identifier) {
        var scaleImpl = Scale.of(flagEntry.get(JsonFlagEntry.SCALE));
        if (scaleImpl == null)
            throw new RuntimeException("invalid scale name for %s: %s".formatted(identifier, flagEntry.get(JsonFlagEntry.SCALE)));

        return new FlagData(flagEntry, buffer, identifier, scaleImpl);
    }


}
