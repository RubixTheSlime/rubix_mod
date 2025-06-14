package io.github.rubixtheslime.rubix.network;

import io.github.rubixtheslime.rubix.redfile.RedfileTag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Map;

public record RedfileResultPacket(
    Map<Long, int[]> data,
    int trialCount,
    boolean splitTags,
    Identifier worldIdentifier
) {

//    ConfidenceInterval confidenceInterval(int samples, double confidenceLevel) {
//        var res = new WilsonScoreInterval().createInterval(samples, (int) totalSamples, confidenceLevel);
//        return new ConfidenceInterval(res.getLowerBound() * sampleMillis, res.getUpperBound() * sampleMillis, confidenceLevel);
//    }

}
