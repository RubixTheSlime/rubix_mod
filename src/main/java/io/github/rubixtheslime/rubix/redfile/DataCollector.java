package io.github.rubixtheslime.rubix.redfile;

import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public abstract class DataCollector {
    private final Lock lock = new ReentrantLock();
    
    public abstract void start(ServerWorld world);

    public abstract void incBlock(BlockPos pos);

    public abstract void incEntity(Entity entity);
    
    public void inc(Object object) {
        if (object instanceof BlockPos pos) incBlock(pos);
        if (object instanceof Entity entity) incEntity(entity);
    }
    
    public Lock getLock() {
        return lock;
    }

    public abstract void finish(long totalSamples, double tickRate, ServerCommandSource source, ServerWorld world);

}
