package io.github.rubixtheslime.rubix.gaygrass;

import java.util.regex.Pattern;

public abstract class Scale {
    public abstract double apply(double x);

    public abstract double inv(double x);

    public abstract double integralOfSquared(double a, double b);

    private static final Pattern SCALE_POW_REGEX = Pattern.compile("pow\\s*(\\d*\\.\\d+|\\d+)");

    public static Scale of(String name) {
        if (name.equals("exp")) return new ExpScale();
        if (name.equals("exp2")) return new Exp2Scale();
        if (name.equals("2exp")) return new DoubleExpScale();
        if (name.equals("perexp")) return new PerExpScale();
        var matcher = SCALE_POW_REGEX.matcher(name);
        if (matcher.matches()) {
            double power = Double.parseDouble(matcher.group(1));
            return new PowScale(power);
        }
        return null;
    }

    private static abstract class Integrable extends Scale {
        protected abstract double integralOfSquared(double x);

        public double integralOfSquared(double a, double b) {
            return integralOfSquared(b) - integralOfSquared(a);
        }
    }

    private static abstract class NonIntegrable extends Scale {
        protected abstract double getStepSize(double a, double b);

        public double integralOfSquared(double a, double b) {
            double stepSize = getStepSize(a, b);
            double total = 0;
            for (double x = a; x < b; x += stepSize) {
                double applied = apply(x);
                total += applied * applied;
            }
            return total * stepSize;
        }
    }

    private static class ExpScale extends Integrable {
        @Override
        public double apply(double x) {
            return Math.exp(x);
        }

        @Override
        public double inv(double x) {
            return Math.log(x);
        }

        @Override
        public double integralOfSquared(double x) {
            return Math.exp(2 * x) / 2;
        }
    }

    private static class Exp2Scale extends NonIntegrable {

        @Override
        public double apply(double x) {
            return Math.exp(x * x);
        }

        @Override
        public double inv(double x) {
            return Math.sqrt(Math.log(x));
        }

        @Override
        protected double getStepSize(double a, double b) {
            return (b - a) / 1000;
        }
    }

    private static class DoubleExpScale extends NonIntegrable {

        @Override
        protected double getStepSize(double a, double b) {
            return (b - a) / 1000;
        }

        @Override
        public double apply(double x) {
            return Math.exp(Math.expm1(x));
        }

        @Override
        public double inv(double x) {
            return Math.log1p(Math.log(x));
        }
    }

    private static final class PowScale extends Integrable {
        private final double power;

        private PowScale(double power) {
            this.power = power;
        }

        @Override
        public double apply(double x) {
            return Math.pow(x, power);
        }

        @Override
        public double inv(double x) {
            return Math.pow(x, 1d / power);
        }

        @Override
        public double integralOfSquared(double x) {
            return Math.pow(x, power * 2 + 1) / (power * 2 + 1);
        }

        public double power() {
            return power;
        }
    }

    private static final class PerExpScale extends Integrable {

        @Override
        protected double integralOfSquared(double x) {
            return -Math.log(-x);
        }

        @Override
        public double apply(double x) {
            return Math.pow(-x, -0.5);
        }

        @Override
        public double inv(double x) {
            return -Math.pow(x, -2);
        }
    }


}
