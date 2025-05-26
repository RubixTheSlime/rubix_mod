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
    void feedback(ServerCommandSource source, MoreMath.MeanAndVar meanAndVar);

    static RedfileSummarizer average(double alpha) {
        return (source, mv) -> {
            var interval = mv.middleInterval(alpha);
            var rangeStr = Util.formatTimeInterval(CompareMode.RANGE, MoreMath.clampZero(interval), true, false);
            source.sendFeedback(() -> Text.translatable("rubix.command.redfile.average_finish", rangeStr), false);
        };
    }

    static RedfileSummarizer compare(CompareMode mode, String name, double alpha) {
        return (source, mv) -> {
            var control = CompareMode.CONTROLS.get(name);
            if (control == null) {
                source.sendFeedback(() -> Text.translatable("rubix.command.redfile.compare.control.none", name), false);
                return;
            }
            var mv2 = mv.copy();
            mv2.sub(control);
            double z = mv2.mean() / mv2.stdDev();
            var interval = mode.normalInterval(mv2, alpha);
            source.sendFeedback(() -> Text.translatable(
                "rubix.command.redfile.compare.result",
                Util.formatTimeInterval(mode, interval, true, true),
                Util.formatTimeDelta(mv2.mean()),
                Util.formatTime(mv2.stdDev()),
                "%.3g".formatted(mode.erfc(z))
            ), false);
//            source.sendFeedback(() -> Text.translatable(
//                "rubix.command.redfile.compare.advanced",
//            ), false);
        };
    }

    static RedfileSummarizer setControl(String name) {
        return (source, mv) -> {
            CompareMode.CONTROLS.put(name, mv);
            source.sendFeedback(() -> Text.translatable("rubix.command.redfile.compare.control.set", name), false);
        };
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

        private static final Map<String, MoreMath.MeanAndVar> CONTROLS = new Object2ObjectOpenHashMap<>();

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
