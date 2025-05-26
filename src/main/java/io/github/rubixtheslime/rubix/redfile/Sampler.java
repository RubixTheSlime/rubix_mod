package io.github.rubixtheslime.rubix.redfile;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.LockSupport;

public class Sampler {
    private static final Sampler INSTANCE = new Sampler();
    // make volatile instead of final to avoid it being optimized out
    // we want to always do roughly the same amount of work for every loop
    private static volatile ConcurrentLinkedDeque<RedfileInstance> defaultList = new ConcurrentLinkedDeque<>();
    private final Thread pollThread = new Thread(this::run);
    private final ConcurrentHashMap<RegistryKey<World>, ConcurrentLinkedDeque<RedfileInstance>> instances = new ConcurrentHashMap<>();
    private int instanceCount = 0;
    private long totalSamples = 0;

    private Sampler() {
        pollThread.start();
    }

    private void run() {
        while (true) {
            if (instanceCount == 0) {
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
            }
            LockSupport.parkNanos(10_000);
//            if (instanceCount == 0) continue;
            var world = RedfileManager.getCurrentWorld();
            var current = RedfileManager.getCurrent();
            ++totalSamples;
            if (current == null) continue;
            var list = world == null ? defaultList : instances.computeIfAbsent(world.getRegistryKey(), x -> new ConcurrentLinkedDeque<>());
            for (var instance : list) {
                instance.inc(current);
            }
        }
    }

    public static Sampler getInstance() {
        return INSTANCE;
    }

    public void bind(World world, RedfileInstance instance) {
        var deque = instances.computeIfAbsent(world.getRegistryKey(), x -> new ConcurrentLinkedDeque<>());
        deque.add(instance);
        ++instanceCount;
        synchronized (this) {
            this.notifyAll();
        }
    }

    public void unbind(World world, RedfileInstance instance) {
        var deque = instances.get(world.getRegistryKey());
        if (deque != null && deque.remove(instance)) {
            --instanceCount;
        }
    }

    public long getTotalSamples() {
        return totalSamples;
    }

    public Thread getPollThread() {
        return pollThread;
    }
}
