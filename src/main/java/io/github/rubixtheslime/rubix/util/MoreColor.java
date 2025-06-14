package io.github.rubixtheslime.rubix.util;

import net.minecraft.util.math.ColorHelper;

public abstract class MoreColor {

    public static int maxComponentRgb(int rgb) {
        return Math.max(Math.max(ColorHelper.getRed(rgb), ColorHelper.getGreen(rgb)), ColorHelper.getBlue(rgb));
    }

    public static int alphaBlend(int top, int bottom) {
        int res = 0;
        double alpha = (double) (top >>> 24) / 255d;
        for (int i = 0; i < 3; ++i) {
            int shift = i * 8;
            double a = (top >>> shift) & 255;
            double b = (bottom >>> shift) & 255;
            double c = Math.sqrt(a * a * alpha + b * b * (1 - alpha));
            res |= (int) c << shift;
        }
        return res;
    }

    public static int[] breakDownRgb(int rgb) {
        return new int[]{ColorHelper.getRed(rgb), ColorHelper.getGreen(rgb), ColorHelper.getBlue(rgb)};
    }

    public static byte[] breakDownRgbBytes(int rgb) {
        return new byte[]{(byte) ColorHelper.getRed(rgb), (byte) ColorHelper.getGreen(rgb), (byte) ColorHelper.getBlue(rgb)};
    }

    public static class QuickRgbShadeBlender {
        private int topR;
        private int topG;
        private int topB;
        private int bottomA;

        private int midR;
        private int midG;
        private int midB;

        private int finalR;
        private int finalG;
        private int finalB;

        private static final int BIG_INVERSE = ((1 << 23) - 1) / 255 + 1;

        public void topArgb(int argb) {
            int topA = ColorHelper.getAlpha(argb) * BIG_INVERSE;
            bottomA = (255 - ColorHelper.getAlpha(argb)) * BIG_INVERSE;
            topR = ColorHelper.getRed(argb) * topA;
            topG = ColorHelper.getGreen(argb) * topA;
            topB = ColorHelper.getBlue(argb) * topA;
        }

        public void bottomRgb(int rgb) {
            midR = (ColorHelper.getRed(rgb) * bottomA + topR) >>> 23;
            midG = (ColorHelper.getGreen(rgb) * bottomA + topG) >>> 23;
            midB = (ColorHelper.getBlue(rgb) * bottomA + topB) >>> 23;
        }

        public static int precompShade(float value) {
            return (int) Math.ceil(value * (1 << 23));
        }

        public void shadePrecomped(int k) {
            finalR = midR * k >>> 23;
            finalG = midG * k >>> 23;
            finalB = midB * k >>> 23;
        }

        public int getFullAbgr() {
            return -1 << 24 | finalB << 16 | finalG << 8 | finalR;
        }

    }

}
