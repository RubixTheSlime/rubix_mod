package io.github.rubixtheslime.rubix.redfile;

import io.github.rubixtheslime.rubix.RubixMod;
import io.github.rubixtheslime.rubix.network.RedfileResultPacket;
import io.github.rubixtheslime.rubix.util.MoreMath;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;
import java.util.List;
import java.util.Map;


public interface DataCollector {
    void start(ServerWorld world);
    void inc(RedfileTag tag, BlockPos pos);
    void finish(ServerCommandSource source, ServerWorld world);
    void split(long trialSamples, double tickRate);

    interface Builder {
        DataCollector get();
    }

    static Builder heatMap(boolean splitTags) {
        return DetailedCollector::new;
    }

    static Builder summary(RedfileSummarizer summarizer, boolean splitTags) {
        return () -> new SummaryCollector(summarizer, splitTags);
    }

    class DetailedCollector implements DataCollector {
        private int trialCount = 0;
        private Map<Long, Long> counts = new Long2LongOpenHashMap();
        private final Map<Long, MoreMath.MeanAndVar> stats = new Long2ObjectOpenHashMap<>();

        @Override
        public void start(ServerWorld world) {
        }

        @Override
        public void inc(RedfileTag tag, BlockPos pos) {
            counts.merge(pos.asLong(), 1L, Long::sum);
        }

        @Override
        public void finish(ServerCommandSource source, ServerWorld world) {
            Map<Long, Long> data = new Long2LongOpenHashMap();
            for (var entry : stats.entrySet()) {
                var value = entry.getValue();
                value.finishPredictive(trialCount);
                data.put(entry.getKey(), value.pack());
            }
            RubixMod.RUBIX_MOD_CHANNEL.serverHandle(source.getPlayer()).send(new RedfileResultPacket(data));
        }

        @Override
        public void split(long trialSamples, double tickRate) {
            trialCount++;
            double scale = tickRate / trialSamples;
            for (var entry : counts.entrySet()) {
                var value = entry.getValue();
                entry.setValue(0L);
                var mv = stats.computeIfAbsent(entry.getKey(), i -> new MoreMath.MeanAndVar());
                mv.update(value * scale, trialCount);
            }
        }
    }

    class SummaryCollector implements DataCollector {
        private int trialCount = 0;
        private final Map<RedfileTag, Long> counts = new Reference2LongOpenHashMap<>();
        private final Map<RedfileTag, MoreMath.MeanAndVar> data = new Reference2ObjectOpenHashMap<>();
        private final RedfileSummarizer summarizer;
        private final boolean spiltTags;

        public SummaryCollector(RedfileSummarizer summarizer, boolean spiltTags) {
            this.summarizer = summarizer;
            this.spiltTags = spiltTags;
        }

        @Override
        public void start(ServerWorld world) {
        }

        @Override
        public void inc(RedfileTag tag, BlockPos pos) {
            counts.merge(RedfileTags.ALL, 1L, Long::sum);
            counts.merge(tag, 1L, Long::sum);
        }

        @Override
        public void finish(ServerCommandSource source, ServerWorld world) {
            if (trialCount < 2) {
                source.sendFeedback(() -> Text.translatable("rubix.command.redfile.one_trial"), false);
                return;
            }
            data.forEach((k, mv) -> mv.finishPredictive(trialCount));
            if (spiltTags) {
                Map<RedfileTag, MoreMath.MeanAndVar> finalData = new Reference2ObjectArrayMap<>(data.size());
                data.entrySet().stream()
                    .sorted(Comparator.comparing(entry -> -entry.getValue().mean()))
                    .forEach(entry -> finalData.put(entry.getKey(), entry.getValue()));
                summarizer.feedback(source, finalData);
            } else {
                summarizer.feedback(source, Map.of(RedfileTags.ALL, data.get(RedfileTags.ALL)));
            }
        }

        @Override
        public void split(long trialSamples, double tickRate) {
            trialCount++;
            for (var entry : counts.entrySet()) {
                double x = (double) entry.getValue() * tickRate / trialSamples;
                data.computeIfAbsent(entry.getKey(), a -> new MoreMath.MeanAndVar())
                    .update(x, trialCount);
            }

            counts.replaceAll((a, b) -> 0L);
        }
    }

}
