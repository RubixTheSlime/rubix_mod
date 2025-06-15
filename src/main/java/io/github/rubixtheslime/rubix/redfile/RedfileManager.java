package io.github.rubixtheslime.rubix.redfile;

import io.github.rubixtheslime.rubix.EnabledMods;
import io.github.rubixtheslime.rubix.ModRegistries;
import io.github.rubixtheslime.rubix.RubixMod;
import io.github.rubixtheslime.rubix.network.RedfileTranslationPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class RedfileManager {
    private static final ReentrantLock WORLD_LOCK = new ReentrantLock();
    private static Thread WORLD_LOCKED_THREAD = null;
    private static final Map<RegistryKey<World>, List<RedfileInstance>> INSTANCES = new HashMap<>();
    private static RedfileTranslationPacket translationPacket = null;
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
        return !INSTANCES.isEmpty();
    }

    public static Object getCurrentRaw() {
        return trackedRaw;
    }

    public static RedfileTracker getCurrent() {
        // this should never be null but under some intense situations it's happened
        return tracked == null ? RedfileTracker.EMPTY : ((RedfileTracker) tracked);
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

    private static boolean shouldLockWorlds(Object world) {
        return world != null && (EnabledMods.SUPPRESSION_BLOCKS || running() || RubixMod.CONFIG.miscOptions.forceSyncWorldThreads());
//        return EnabledMods.SUPPRESSION_BLOCKS || running() || RubixMod.CONFIG.miscOptions.forceSyncWorldThreads();
    }

    public static void lockWorld(Object world) {
        if (shouldLockWorlds(world)) {
            WORLD_LOCK.lock();
            WORLD_LOCKED_THREAD = Thread.currentThread();
        }
    }

    public static void unlockWorld(Object world) {
        if (Thread.currentThread() == WORLD_LOCKED_THREAD) {
            WORLD_LOCKED_THREAD = null;

            WORLD_LOCK.unlock();
        }
    }

    public static void enterWorld(ServerWorld world) {
        lockWorld(world);
        currentWorld = world;
    }

    public static void exitWorld(ServerWorld world) {
        currentWorld = null;
        unlockWorld(world);
    }

    public static void tickWorld(ServerWorld world) {
        var entry = INSTANCES.get(world.getRegistryKey());
        if (entry == null) return;
        entry.forEach(RedfileInstance::tick);
        entry.removeIf(instance -> !instance.isRunning());
        if (entry.isEmpty()) INSTANCES.remove(world.getRegistryKey());
    }

    public static boolean tryStop() {
        if (INSTANCES.isEmpty()) return false;
        INSTANCES.forEach((k, v) -> v.forEach(RedfileInstance::stop));
        INSTANCES.clear();
        return true;
    }

    public static RedfileTranslationPacket getTranslationPacket() {
        if (translationPacket == null) {
            translationPacket = new RedfileTranslationPacket(ModRegistries.REDFILE_TAG.stream().map(tag -> tag.id().toString()).toList());
        }
        return translationPacket;
    }
}
