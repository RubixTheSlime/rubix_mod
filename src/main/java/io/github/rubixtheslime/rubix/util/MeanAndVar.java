package io.github.rubixtheslime.rubix.util;

import org.apache.commons.math3.stat.interval.ConfidenceInterval;

public class MeanAndVar {
    double mean = 0;
    double variance = 0;

    public MeanAndVar() {
    }

    public MeanAndVar(double mean, double variance) {
        this.mean = mean;
        this.variance = variance;
    }

    public static MeanAndVar unpack(long packed) {
        return new MeanAndVar(Float.intBitsToFloat((int) (packed >> 32)), Float.intBitsToFloat((int) packed));
    }

    public ConfidenceInterval middleInterval(double alpha) {
        return MoreMath.normalMiddleInterval(mean, stdDev(), alpha);
    }

    public void add(MeanAndVar other) {
        mean += other.mean;
        variance += other.variance;
    }

    public void sub(MeanAndVar other) {
        mean -= other.mean;
        variance += other.variance;
    }

    /// finalize the results for the case of estimating the mean
    public void finishPredictive(long n) {
        variance /= (n - 1) * n;
    }

    public void update(double x, long n) {
        double d = x - mean;
        mean += d / n;
        double d2 = x - mean;
        variance += d * d2;
    }

    public long pack() {
        return (long) Float.floatToRawIntBits((float) mean) << 32 | Float.floatToRawIntBits((float) variance);
    }

    public double mean() {
        return mean;
    }

    public double variance() {
        return variance;
    }

    public double stdDev() {
        return Math.sqrt(variance);
    }

    public MeanAndVar copy() {
        return new MeanAndVar(mean, variance);
    }

    public boolean isEmpty() {
        return mean == 0 && variance == 0;
    }

    public void multiplyVariance(double amount) {
        variance *= amount;
    }
}
