package io.github.rubixtheslime.rubix.util;

import net.minecraft.util.math.ColorHelper;

public class MoreColor {

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

    public static class QuickRgbBlender {
        private final int[] topPartsScaled = new int[3];
        private final int bottomAlphaScaled;


        private QuickRgbBlender(int topRedScaled, int topGreenScaled, int topBlueScaled, int bottomAlphaScaled) {
            topPartsScaled[0] = topRedScaled;
            topPartsScaled[1] = topGreenScaled;
            topPartsScaled[2] = topBlueScaled;
            this.bottomAlphaScaled = bottomAlphaScaled;
        }

        public static QuickRgbBlender of(int red, int green, int blue, int alpha) {
            int topAlphaScale = ((alpha << 23) - 1) / 255 + 1;
            return new QuickRgbBlender(
                red * topAlphaScale,
                green * topAlphaScale,
                blue * topAlphaScale,
                (((255 - alpha) << 23) - 1) / 255 + 1
            );
        }

        public static QuickRgbBlender of(int argb) {
            return of(ColorHelper.getRed(argb), ColorHelper.getGreen(argb), ColorHelper.getBlue(argb), ColorHelper.getAlpha(argb));
        }

        public void blend(int[] parts) {
            for (int i = 0; i < 3; i++) {
                parts[i] = (parts[i] * bottomAlphaScaled + topPartsScaled[i]) >> 23;
            }
        }
    }

}
