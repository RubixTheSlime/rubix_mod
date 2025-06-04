package io.github.rubixtheslime.rubix.redfile;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class RedfileManager {
    private static final ReentrantLock RUN_LOCK = new ReentrantLock();
    private static final ReentrantLock STOP_LOCK = new ReentrantLock();
    private static final Map<RegistryKey<World>, List<RedfileInstance>> INSTANCES = new HashMap<>();
    private static @NotNull Object trackedRaw = RedfileTracker.EMPTY;
    private static volatile @NotNull Object tracked = RedfileTracker.EMPTY;
    private static volatile ServerWorld currentWorld = null;

    public static ServerWorld getCurrentWorld() {
        return currentWorld;
    }

    public static void enter(Object o) {
        trackedRaw = o;
        tracked = o;
    }

    public static void enterLeaf(Object o) {
        tracked = o;
    }

    public static void exit() {
        enter(RedfileTracker.EMPTY);
    }

    public static void exitLeaf() {
        enterLeaf(RedfileTracker.EMPTY);
    }

    public static boolean running() {
        return RUN_LOCK.isLocked() || STOP_LOCK.isLocked();
    }

    public static Object getCurrentRaw() {
        return trackedRaw;
    }

    public static RedfileTracker getCurrent() {
        return ((RedfileTracker) tracked);
    }

    public static boolean tryStart(
        BlockBox box,
        RedfileEndCondition.Builder runEndCondition,
        RedfileEndCondition.Builder trialEndCondition,
        DataCollector.Builder collectorBuilder,
        boolean doLoad,
        boolean doSprint,
        ServerCommandSource source
    ) {
        return INSTANCES
            .computeIfAbsent(source.getWorld().getRegistryKey(), x -> new ArrayList<>())
            .add(RedfileInstance.start(box, runEndCondition, trialEndCondition, collectorBuilder, doLoad, doSprint, source));
    }

    public static void enterAndTickWorld(ServerWorld world) {
        currentWorld = world;
        var entry = INSTANCES.get(world.getRegistryKey());
        if (entry == null) return;
        entry.forEach(RedfileInstance::tick);
        entry.removeIf(instance -> !instance.isRunning());
        if (entry.isEmpty()) INSTANCES.remove(world.getRegistryKey());
    }

    public static void exitWorld() {
        currentWorld = null;
    }

    public static boolean tryStop() {
        if (INSTANCES.isEmpty()) return false;
        INSTANCES.forEach((k, v) -> v.forEach(RedfileInstance::stop));
        INSTANCES.clear();
        return true;
    }
}
