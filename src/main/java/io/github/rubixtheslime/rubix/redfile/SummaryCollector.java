package io.github.rubixtheslime.rubix.redfile;

import io.github.rubixtheslime.rubix.util.MoreMath;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.math3.stat.interval.WilsonScoreInterval;

import static io.github.rubixtheslime.rubix.util.Util.formatTime;

public class SummaryCollector extends DataCollector {
    private long count = 0;

    @Override
    public void start(ServerWorld world) {
    }

    @Override
    public void inc(BlockPos pos) {
        ++count;
    }

    @Override
    public void finish(long totalSamples, double tickRate, ServerCommandSource source, ServerWorld world) {
        if (totalSamples == 0) {
            source.sendFeedback(() -> Text.translatable("rubix.command.redfile.fail_finish"), false);
            return;
        }
//        double averageLag = ((double) count) / totalSamples * sampleMillis;
//        String display = formatTime(averageLag);
        var interval = MoreMath.longWilsonInterval(totalSamples, count, 0.95d);

        source.sendFeedback(() -> Text.translatable("rubix.command.redfile.summary_finish", formatTime(interval.getLowerBound() * tickRate), formatTime(interval.getUpperBound() * tickRate)), false);
    }

}
