package io.github.rubixtheslime.rubix.render;

import net.minecraft.util.math.ColorHelper;

public enum ColorMode {
    RED_GREEN_BLUE(3, 0, 1, 2) {
        @Override
        public double[] toDoubleComponents(int color) {
            return new double[]{ColorHelper.getRedFloat(color), ColorHelper.getGreenFloat(color), ColorHelper.getBlueFloat(color)};
        }
    },
    BLUE_YELLOW(2, 1, 1, 0) {
        @Override
        public double[] toDoubleComponents(int color) {
            return ColorMode.getComponents2(color, ColorHelper.getBlueFloat(color));
        }
    },
    RED_CYAN(2, 0, 1, 1) {
        @Override
        public double[] toDoubleComponents(int color) {
            return ColorMode.getComponents2(color, ColorHelper.getRedFloat(color));
        }
    },
    GREEN_MAGENTA(2, 1, 0, 1) {
        @Override
        public double[] toDoubleComponents(int color) {
            return ColorMode.getComponents2(color, ColorHelper.getGreenFloat(color));
        }
    },
    GREY(1, 0, 0, 0) {
        @Override
        public double[] toDoubleComponents(int color) {
            return new double[]{ColorMode.getSum(color) * (1f / 3)};
        }
    };
    public final int components;
    private final int rIndex;
    private final int gIndex;
    private final int bIndex;

    ColorMode(int components, int rIndex, int gIndex, int bIndex) {
        this.components = components;
        this.rIndex = rIndex;
        this.gIndex = gIndex;
        this.bIndex = bIndex;
    }

    public int fromComponents(int[] components) {
        return ColorHelper.getArgb(components[rIndex], components[gIndex], components[bIndex]);
    }

    public int fromComponents(float[] components) {
        return ColorHelper.fromFloats(1F, components[rIndex], components[gIndex], components[bIndex]);
    }

    public int fromComponents(double[] components) {
        return ColorHelper.fromFloats(1F, (float) components[rIndex], (float) components[gIndex], (float) components[bIndex]);
    }

    abstract public double[] toDoubleComponents(int color);

    private static double[] getComponents2(int color, float part) {
        return new double[]{part, (getSum(color) - part) * 0.5f};
    }

    private static float getSum(int color) {
        return ColorHelper.getRedFloat(color) + ColorHelper.getGreenFloat(color) + ColorHelper.getBlueFloat(color);
    }

}
