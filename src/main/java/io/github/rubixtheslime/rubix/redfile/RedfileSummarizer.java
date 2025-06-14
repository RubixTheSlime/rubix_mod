package io.github.rubixtheslime.rubix.redfile;

import io.github.rubixtheslime.rubix.util.MeanAndVar;
import io.github.rubixtheslime.rubix.util.MoreMath;
import io.github.rubixtheslime.rubix.util.Util;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import org.apache.commons.math3.stat.interval.ConfidenceInterval;

import java.util.Map;

public interface RedfileSummarizer {
    void feedback(ServerCommandSource source, TaggedStats.Display stats);

    Map<String, TaggedStats.Display> CONTROLS = new Object2ObjectOpenHashMap<>();

    static RedfileSummarizer average(double confidencePercent) {
        var confidence = 1d / 100 * confidencePercent;
        return (source, display) -> {
            var sumRangeStr = Util.formatTimeInterval(CompareMode.RANGE, MoreMath.clampZero(display.sum().middleInterval(confidence)), true, false);
            var fullText = Text.translatable("rubix.command.redfile.average_finish", sumRangeStr);
            if (display.shouldBreakDown()) {
                for (var entry : display.data().entrySet()) {
                    var rangeStr = Util.formatTimeInterval(CompareMode.RANGE, MoreMath.clampZero(entry.getValue().middleInterval(confidence)), false, false);
                    fullText.append("\n");
                    fullText.append(Text.translatable("rubix.command.redfile.with_tag", entry.getKey().getName(), rangeStr));
                }
            }
            source.sendFeedback(() -> fullText, false);
        };
    }

    static RedfileSummarizer compare(CompareMode mode, String name, double confidencePercent) {
        var confidence = 1d / 100 * confidencePercent;
        return (source, display) -> {
            var control = CONTROLS.get(name);
            if (control == null) {
                source.sendFeedback(() -> Text.translatable("rubix.command.redfile.compare.control.none", name), false);
                return;
            }
            var sum = display.sum().copy();
            sum.sub(control.sum());
            var sumRangeStr = Util.formatTimeInterval(CompareMode.RANGE, mode.normalInterval(sum, confidence), true, true);
            var fullText = Text.translatable("rubix.command.redfile.average_finish", sumRangeStr);
            if (display.shouldBreakDown()) {
                control.stats().addEmpties(control.stats());
                for (var entry : display.data().entrySet()) {
                    var controlMv = control.stats().get(entry.getKey());
                    var mv = entry.getValue().copy();
                    mv.sub(controlMv);
                    var rangeStr = Util.formatTimeInterval(mode, mode.normalInterval(mv, confidence), false, true);
                    Text text = Text.translatable("rubix.command.redfile.with_tag", entry.getKey().getName(), rangeStr);
                    fullText.append("\n");
                    fullText.append(text);
                }
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

    enum CompareMode implements StringIdentifiable {
        LOW_BOUND("above") {
            @Override
            public ConfidenceInterval normalInterval(MeanAndVar mv, double confidence) {
                return MoreMath.normalLowBoundInterval(mv.mean(), mv.stdDev(), confidence);
            }
        },
        HIGH_BOUND("below") {
            @Override
            public ConfidenceInterval normalInterval(MeanAndVar mv, double confidence) {
                return MoreMath.normalHighBoundInterval(mv.mean(), mv.stdDev(), confidence);
            }
        },
        RANGE("middle") {
            @Override
            public ConfidenceInterval normalInterval(MeanAndVar mv, double confidence) {
                return MoreMath.normalMiddleInterval(mv.mean(), mv.stdDev(), confidence);
            }
        };

        private final String name;

        CompareMode(String name) {
            this.name = name;
        }

        public abstract ConfidenceInterval normalInterval(MeanAndVar mv, double confidence);

        @Override
        public String asString() {
            return name;
        }
    }
}
