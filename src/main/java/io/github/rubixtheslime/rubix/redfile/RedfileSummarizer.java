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
    void feedback(ServerCommandSource source, Map<RedfileTag, MoreMath.MeanAndVar> data);

    static RedfileSummarizer average(double alpha) {
        return (source, data) -> {
            var fullText = Text.empty();
            boolean multi = false;
            for (var entry : data.entrySet()) {
                var interval = entry.getValue().middleInterval(alpha);
                var rangeStr = Util.formatTimeInterval(CompareMode.RANGE, MoreMath.clampZero(interval), entry.getKey() == RedfileTags.ALL, false);
                Text text = entry.getKey() == RedfileTags.ALL
                    ? Text.translatable("rubix.command.redfile.average_finish", rangeStr)
                    : Text.translatable("rubix.command.redfile.with_tag", Text.translatable("redfile.tag." + entry.getKey()), rangeStr);
                if (multi) fullText.append("\n");
                multi = true;
                fullText.append(text);
            }
            source.sendFeedback(() -> fullText, false);
        };
    }

    static RedfileSummarizer compare(CompareMode mode, String name, double alpha) {
        return (source, data) -> {
            var control = ControlEntry.CONTROLS.get(name);
            if (control == null) {
                source.sendFeedback(() -> Text.translatable("rubix.command.redfile.compare.control.none", name), false);
                return;
            }
            for (var entry : data.entrySet()) {
                var controlMv = control.data.get(entry.getKey());
                if (controlMv == null) continue;
                var mv = entry.getValue().copy();
                mv.sub(controlMv);
                double z = mv.mean() / mv.stdDev();
                var interval = mode.normalInterval(mv, alpha);
                var rangeStr = Util.formatTimeInterval(mode, interval, entry.getKey() == RedfileTags.ALL, true);

                Text text;
                if (entry.getKey() == RedfileTags.ALL) {
                    text = Text.translatable("rubix.command.redfile.average_finish", rangeStr,
                        Util.formatTimeDelta(mv.mean()),
                        Util.formatTime(mv.stdDev()),
                        "%.3g".formatted(mode.erfc(z))
                        );
                } else {
                    text = Text.translatable("rubix.command.redfile.with_tag", Text.translatable("redfile.tag." + entry.getKey()), rangeStr);
                }
                source.sendFeedback(() -> text, false);
            }
        };
    }

    static RedfileSummarizer setControl(String name) {
        return (source, data) -> {
            ControlEntry.CONTROLS.put(name, new ControlEntry(data));
            source.sendFeedback(() -> Text.translatable("rubix.command.redfile.compare.control.set", name), false);
        };
    }

    class ControlEntry {
        private static final Map<String, ControlEntry> CONTROLS = new Object2ObjectOpenHashMap<>();
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
