package io.github.rubixtheslime.rubix.redfile;

import io.github.rubixtheslime.rubix.util.MoreMath;
import io.github.rubixtheslime.rubix.util.Util;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import org.apache.commons.math3.special.Erf;
import org.apache.commons.math3.stat.interval.ConfidenceInterval;

import java.util.Map;

import static io.github.rubixtheslime.rubix.util.Util.formatTime;

public interface RedfileSummarizer {
    void feedback(ServerCommandSource source, TaggedStats.Display stats);

    Map<String, TaggedStats.Display> CONTROLS = new Object2ObjectOpenHashMap<>();

    static RedfileSummarizer average(double alpha) {
        return (source, display) -> {
            var sumRangeStr = Util.formatTimeInterval(CompareMode.RANGE, MoreMath.clampZero(display.sum().middleInterval(alpha)), true, false);
            var fullText = Text.translatable("rubix.command.redfile.average_finish", sumRangeStr);
            for (var entry : display.data().entrySet()) {
                fullText.append("\n");
                var rangeStr = Util.formatTimeInterval(CompareMode.RANGE, MoreMath.clampZero(entry.getValue().middleInterval(alpha)), false, false);
                fullText.append(Text.translatable("rubix.command.redfile.with_tag", entry.getKey().getName(), rangeStr));
            }
            source.sendFeedback(() -> fullText, false);
        };
    }

    static RedfileSummarizer compare(CompareMode mode, String name, double alpha) {
        return (source, display) -> {
            var control = CONTROLS.get(name);
            if (control == null) {
                source.sendFeedback(() -> Text.translatable("rubix.command.redfile.compare.control.none", name), false);
                return;
            }
            var sumRangeStr = Util.formatTimeInterval(CompareMode.RANGE, MoreMath.clampZero(display.sum().middleInterval(alpha)), true, true);
            var fullText = Text.translatable("rubix.command.redfile.average_finish", sumRangeStr);
            for (var entry : display.data().entrySet()) {
                var controlMv = control.stats().get(entry.getKey());
                var mv = entry.getValue().copy();
                mv.sub(controlMv);
                var rangeStr = Util.formatTimeInterval(mode, mode.normalInterval(mv, alpha), false, true);
                Text text = Text.translatable("rubix.command.redfile.with_tag", entry.getKey().getName(), rangeStr);
                fullText.append("\n");
                fullText.append(text);
            }
            source.sendFeedback(() -> fullText, false);
        };
    }

    static RedfileSummarizer setControl(String name) {
        return (source, display) -> {
            CONTROLS.put(name, display);
            source.sendFeedback(() -> Text.translatable("rubix.command.redfile.compare.control.set", name), false);
        };
    }

    class ControlEntry {
        private final Map<RedfileTag, MoreMath.MeanAndVar> data;

        private ControlEntry(Map<RedfileTag, MoreMath.MeanAndVar> data) {
            this.data = data;
        }
    }

    enum CompareMode implements StringIdentifiable {
        LOW_BOUND("above") {
            @Override
            public ConfidenceInterval normalInterval(MoreMath.MeanAndVar mv, double alpha) {
                return MoreMath.normalLowBoundInterval(mv.mean(), mv.stdDev(), alpha);
            }

            @Override
            public double erfc(double z) {
                return Erf.erfc(-z) * 0.5;
            }
        },
        HIGH_BOUND("below") {
            @Override
            public ConfidenceInterval normalInterval(MoreMath.MeanAndVar mv, double alpha) {
                return MoreMath.normalHighBoundInterval(mv.mean(), mv.stdDev(), alpha);
            }

            @Override
            public double erfc(double z) {
                return Erf.erfc(z) * 0.5;
            }
        },
        RANGE("middle") {
            @Override
            public ConfidenceInterval normalInterval(MoreMath.MeanAndVar mv, double alpha) {
                return MoreMath.normalMiddleInterval(mv.mean(), mv.stdDev(), alpha);
            }

            @Override
            public double erfc(double z) {
                return Erf.erfc(Math.abs(z));
            }
        };


        private final String name;

        CompareMode(String name) {
            this.name = name;
        }

        public abstract ConfidenceInterval normalInterval(MoreMath.MeanAndVar mv, double alpha);

        public abstract double erfc(double z);

        @Override
        public String asString() {
            return name;
        }
    }
}
