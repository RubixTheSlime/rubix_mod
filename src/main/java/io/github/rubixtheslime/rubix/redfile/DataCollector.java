package io.github.rubixtheslime.rubix.redfile;

import io.github.rubixtheslime.rubix.RubixMod;
import io.github.rubixtheslime.rubix.network.RedfileResultPacket;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.Map;


public abstract class DataCollector {
    protected long trialCount = 0;

    public abstract void start(ServerWorld world);

    public abstract void inc(RedfileTag tag, BlockPos pos);

    public void split(long trialSamples, double tickRate) {
        trialCount++;
        splitInner(trialSamples, tickRate / trialSamples);
    }

    protected abstract void splitInner(long trialSamples, double sampleWeight);

    public boolean finish(ServerCommandSource source, ServerWorld world) {
        if (trialCount < 2) {
            source.sendFeedback(() -> Text.translatable("rubix.command.redfile.one_trial"), false);
            return false;
        }
        return true;
    }

    public interface Builder {
        DataCollector get();
    }

    public static Builder heatMap(boolean splitTags) {
        return () -> new DetailedCollector(splitTags);
    }

    public static Builder summary(RedfileSummarizer summarizer, boolean splitTags) {
        return () -> new SummaryCollector(summarizer, splitTags);
    }

    public static class DetailedCollector extends DataCollector {
        private final boolean splitTags;
        private final Map<Long, TaggedStats.Builder> builders = new Long2ObjectOpenHashMap<>();
        private final Map<Long, TaggedStats> stats = new Long2ObjectOpenHashMap<>();

        public DetailedCollector(boolean splitTags) {
            this.splitTags = splitTags;
        }

        @Override
        public void start(ServerWorld world) {
        }

        @Override
        public void inc(RedfileTag tag, BlockPos pos) {
            builders
                .computeIfAbsent(pos.asLong(), a -> TaggedStats.Builder.create(false, splitTags))
                .inc(tag);
        }

        @Override
        public void splitInner(long trialSamples, double sampleWeight) {
            for (var entry : builders.entrySet()) {
                var statsEntry = stats.computeIfAbsent(entry.getKey(), a -> TaggedStats.create(false));
                entry.getValue().commit(statsEntry, sampleWeight, trialCount);
            }
        }

        @Override
        public boolean finish(ServerCommandSource source, ServerWorld world) {
            if (!super.finish(source, world)) return false;
            Map<Long, int[]> data = new Long2ObjectOpenHashMap<>();
            for (var entry : stats.entrySet()) {
                var value = entry.getValue();
                value.finishCollecting(trialCount);
                data.put(entry.getKey(), value.pack());
            }
            RubixMod.RUBIX_MOD_CHANNEL.serverHandle(source.getPlayer()).send(new RedfileResultPacket(data, splitTags, world.getRegistryKey().getValue()));
            return true;
        }

    }

    public static class SummaryCollector extends DataCollector {
        private final RedfileSummarizer summarizer;
        private final TaggedStats.Builder builder;
        private final TaggedStats stats = TaggedStats.create(false);

        public SummaryCollector(RedfileSummarizer summarizer, boolean splitTags) {
            this.summarizer = summarizer;
            this.builder = TaggedStats.Builder.create(false, splitTags);
        }

        @Override
        public void start(ServerWorld world) {
        }

        @Override
        public void inc(RedfileTag tag, BlockPos pos) {
            builder.inc(tag);
        }

        @Override
        public void splitInner(long trialSamples, double sampleWeight) {
            builder.commit(stats, sampleWeight, trialCount);
        }

        @Override
        public boolean finish(ServerCommandSource source, ServerWorld world) {
            if (!super.finish(source, world)) return false;
            stats.finishCollecting(trialCount);
            summarizer.feedback(source, stats.getDisplay(null));
            return true;
        }

    }

}
