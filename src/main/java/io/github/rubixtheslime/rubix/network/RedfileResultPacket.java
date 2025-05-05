package io.github.rubixtheslime.rubix.network;

import java.util.Map;

public record RedfileResultPacket(
    Map<Long, Long> data,
    long totalSamples,
    double tickMillis) {

//    ConfidenceInterval confidenceInterval(int samples, double confidenceLevel) {
//        var res = new WilsonScoreInterval().createInterval(samples, (int) totalSamples, confidenceLevel);
//        return new ConfidenceInterval(res.getLowerBound() * sampleMillis, res.getUpperBound() * sampleMillis, confidenceLevel);
//    }

}
