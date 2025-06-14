package io.github.rubixtheslime.rubix.redfile;

import io.github.rubixtheslime.rubix.ModRegistries;
import io.github.rubixtheslime.rubix.render.ColorMode;
import net.minecraft.client.gui.hud.debug.PieChart;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;

import java.awt.*;

public class RedfileTag {

    public static RedfileTag of() {
        return new RedfileTag();
    }

    public double[] getInitialPoint(ColorMode mode) {
        Random random = new Xoroshiro128PlusPlusRandom(this.id().hashCode());
        var res = new double[mode.components];
        while (true) {
            double hypot2 = 0;
            for (int i = 0; i < res.length; i++) {
                var x = random.nextDouble() * 2 - 1;
                hypot2 += x * x;
                res[i] = x;
            }
            if (hypot2 <= 1) return res;
        }
    }

    public Color getColor() {
        return new Color((this.id().hashCode() & 0xaaaaaa) + 0x333333);
    }

    public int index() {
        return ModRegistries.REDFILE_TAG.getRawId(this);
    }

    public Identifier id() {
        return ModRegistries.REDFILE_TAG.getId(this);
    }

    @Override
    public String toString() {
        return id().toTranslationKey();
    }

    public Text getName() {
        return Text.translatable("redfile.tag." + toString());
    }

}
