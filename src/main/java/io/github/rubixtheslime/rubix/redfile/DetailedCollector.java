package io.github.rubixtheslime.rubix.redfile;

import io.github.rubixtheslime.rubix.RubixMod;
import io.github.rubixtheslime.rubix.network.RedfileResultPacket;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public class DetailedCollector extends DataCollector {
//    private static final Entry defaultEntry = new Entry(0);
    private final Map<Long, Long> data = new Long2LongOpenHashMap();

    @Override
    public void start(ServerWorld world) {
    }

    @Override
    public void inc(BlockPos pos) {
        data.merge(pos.asLong(), 1L, Long::sum);
    }

    @Override
    public void finish(long totalSamples, double tickRate, ServerCommandSource source, ServerWorld world) {
        RubixMod.RUBIX_MOD_CHANNEL.serverHandle(source.getPlayer()).send(
            new RedfileResultPacket(data, totalSamples, tickRate)
        );
    }
}
