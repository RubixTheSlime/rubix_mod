package io.github.rubixtheslime.rubix.util;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.interval.ConfidenceInterval;
import org.apache.commons.math3.util.FastMath;

public class MoreMath {

    // copied from apache commons math3, but with longs instead of ints
    public static ConfidenceInterval longWilsonInterval(long numberOfTrials, long numberOfSuccesses, double confidenceLevel) {
        if (numberOfTrials <= 0 || numberOfSuccesses < 0 || confidenceLevel <= 0 || confidenceLevel >= 1) {
            throw new RuntimeException("bad parameters for interval");
        }
        final double alpha = (1.0 - confidenceLevel) / 2;
        final NormalDistribution normalDistribution = new NormalDistribution();
        final double z = normalDistribution.inverseCumulativeProbability(1 - alpha);
        final double zSquared = FastMath.pow(z, 2);
        final double mean = (double) numberOfSuccesses / (double) numberOfTrials;

        final double factor = 1.0 / (1 + (1.0 / numberOfTrials) * zSquared);
        final double modifiedSuccessRatio = mean + (1.0 / (2 * numberOfTrials)) * zSquared;
        final double difference = z *
            FastMath.sqrt(1.0 / numberOfTrials * mean * (1 - mean) +
                (1.0 / (4 * FastMath.pow(numberOfTrials, 2)) * zSquared));

        final double lowerBound = factor * (modifiedSuccessRatio - difference);
        final double upperBound = factor * (modifiedSuccessRatio + difference);
        return new ConfidenceInterval(lowerBound, upperBound, confidenceLevel);
    }
}
