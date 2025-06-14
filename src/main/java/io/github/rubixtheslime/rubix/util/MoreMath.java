package io.github.rubixtheslime.rubix.util;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.special.Erf;
import org.apache.commons.math3.stat.interval.ConfidenceInterval;
import org.apache.commons.math3.util.FastMath;

import java.math.BigDecimal;

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

    public static ConfidenceInterval clampZero(ConfidenceInterval interval) {
        if (interval == null) return null;
        return new ConfidenceInterval(Math.max(interval.getLowerBound(), 0), interval.getUpperBound() == 0 ? Double.MIN_NORMAL : interval.getUpperBound(), interval.getConfidenceLevel());
    }

    public static ConfidenceInterval normalHighBoundInterval(double mean, double stdDev, double alpha) {
        double z = Erf.erfInv(1 - alpha * 2);
        double lowerBound = Float.NEGATIVE_INFINITY;
        double upperBound = mean + stdDev * z;
        if (lowerBound == upperBound) upperBound += Math.ulp(upperBound);
        return new ConfidenceInterval(lowerBound, upperBound, 1 - alpha);
    }

    public static ConfidenceInterval normalLowBoundInterval(double mean, double stdDev, double confidence) {
        double z = Erf.erfInv(confidence * 2 - 1);
        double lowerBound = mean - stdDev * z;
        double upperBound = Float.POSITIVE_INFINITY;
        if (lowerBound == upperBound) lowerBound -= Math.ulp(lowerBound);
        return new ConfidenceInterval(lowerBound, upperBound, confidence);
    }

    public static ConfidenceInterval normalMiddleInterval(double mean, double stdDev, double confidence) {
        double z = Erf.erfInv(confidence);
        double lowerBound = mean - stdDev * z;
        double upperBound = mean + stdDev * z;
        if (lowerBound == upperBound) upperBound += Math.ulp(upperBound);
        if (lowerBound == upperBound) lowerBound -= Math.ulp(lowerBound);
        return new ConfidenceInterval(lowerBound, upperBound, confidence);
    }

    public static class MeanVarAcc {
        BigDecimal mean = BigDecimal.ZERO;
        BigDecimal variance = BigDecimal.ZERO;

        public void add(MeanAndVar meanAndVar) {
            if (meanAndVar == null) return;
            mean = mean.add(BigDecimal.valueOf(meanAndVar.mean));
            variance = variance.add(BigDecimal.valueOf(meanAndVar.variance));
        }

        public MeanAndVar finish() {
            return new MeanAndVar(mean.doubleValue(), variance.doubleValue());
        }
    }
}
