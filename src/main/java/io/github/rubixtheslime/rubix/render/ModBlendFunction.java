package io.github.rubixtheslime.rubix.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;

public class ModBlendFunction {
    public static final BlendFunction TRANSLUCENT_HIVE = new BlendFunction(
        SourceFactor.CONSTANT_ALPHA, DestFactor.ONE_MINUS_CONSTANT_ALPHA, SourceFactor.ONE, DestFactor.ONE_MINUS_CONSTANT_ALPHA
    );
}
