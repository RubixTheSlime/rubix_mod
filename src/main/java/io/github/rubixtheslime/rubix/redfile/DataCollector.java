package io.github.rubixtheslime.rubix.redfile;

import io.github.rubixtheslime.rubix.RubixMod;
import io.github.rubixtheslime.rubix.network.RedfileResultPacket;
import io.github.rubixtheslime.rubix.util.MoreMath;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.Map;


public interface DataCollector {
    void start(ServerWorld world);
    void inc(BlockPos pos);
    void finish(ServerCommandSource source, ServerWorld world);
    void split(long trialSamples, double tickRate);

    interface Builder {
        DataCollector get();
    }

    static Builder heatMap() {
        return DetailedCollector::new;
    }

    static Builder summary(RedfileSummarizer summarizer) {
        return () -> new SummaryCollector(summarizer);
    }

    class DetailedCollector implements DataCollector {
        private int trialCount = 0;
        private Map<Long, Long> counts = new Long2LongOpenHashMap();
        private final Map<Long, MoreMath.MeanAndVar> stats = new Long2ObjectOpenHashMap<>();

        @Override
        public void start(ServerWorld world) {
        }

        @Override
        public void inc(BlockPos pos) {
            counts.merge(pos.asLong(), 1L, Long::sum);
        }

        @Override
        public void finish(ServerCommandSource source, ServerWorld world) {
            Map<Long, Long> data = new Long2LongOpenHashMap();
            for (var entry : stats.entrySet()) {
                entry.getValue().finishPredictive(trialCount);
                data.put(entry.getKey(), entry.getValue().pack());
            }
            RubixMod.RUBIX_MOD_CHANNEL.serverHandle(source.getPlayer()).send(new RedfileResultPacket(data));
        }

        @Override
        public void split(long trialSamples, double tickRate) {
            var oldCounts = counts;
            counts = new Long2LongOpenHashMap();
            trialCount++;
            double scale = tickRate / trialSamples;
            for (var entry : oldCounts.entrySet()) {
                var x = stats.computeIfAbsent(entry.getKey(), i -> new MoreMath.MeanAndVar());
                x.update(entry.getValue() * scale, trialCount);
            }
        }
    }

    class SummaryCollector implements DataCollector {
        private long count = 0;
        private int trialCount = 0;
        private final MoreMath.MeanAndVar meanAndVar = new MoreMath.MeanAndVar();
        private final RedfileSummarizer summarizer;

        public SummaryCollector(RedfileSummarizer summarizer) {
            this.summarizer = summarizer;
        }

        @Override
        public void start(ServerWorld world) {
        }

        @Override
        public void inc(BlockPos pos) {
            ++count;
        }

        @Override
        public void finish(ServerCommandSource source, ServerWorld world) {
            if (trialCount < 2) {
                source.sendFeedback(() -> Text.translatable("rubix.command.redfile.one_trial"), false);
                return;
            }
            meanAndVar.finishPredictive(trialCount);
            summarizer.feedback(source, meanAndVar);
        }

        @Override
        public void split(long trialSamples, double tickRate) {
            double x = (double) count * tickRate / trialSamples;
            trialCount++;
            meanAndVar.update(x, trialCount);

            count = 0;
        }
    }

}
