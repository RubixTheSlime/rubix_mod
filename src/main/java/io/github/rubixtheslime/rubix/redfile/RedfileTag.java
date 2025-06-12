package io.github.rubixtheslime.rubix.redfile;

import io.github.rubixtheslime.rubix.ModRegistries;
import net.minecraft.client.gui.hud.debug.PieChart;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;

public class RedfileTag {

    public static RedfileTag of() {
        return new RedfileTag();
    }

    public Color getColor() {
        return new Color((this.id().hashCode() & 0xaaaaaa) + 0x444444);
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
