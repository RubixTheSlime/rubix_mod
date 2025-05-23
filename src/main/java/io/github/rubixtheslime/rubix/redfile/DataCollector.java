package io.github.rubixtheslime.rubix.redfile;

import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public abstract class DataCollector {
    public abstract void start(ServerWorld world);

    public abstract void inc(BlockPos pos);

    public abstract void finish(long totalSamples, double tickRate, ServerCommandSource source, ServerWorld world);

}
