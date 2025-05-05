package io.github.rubixtheslime.rubix.redfile;

import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockEntityTickInvoker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class RedfileManager {
    private static final ReentrantLock RUN_LOCK = new ReentrantLock();
    private static final ReentrantLock STOP_LOCK = new ReentrantLock();
    private static final Map<RegistryKey<World>, List<RedfileInstance>> INSTANCES = new HashMap<>();
    private static volatile Object tracked = null;
    private static volatile ServerWorld currentWorld = null;

    public static ServerWorld getCurrentWorld() {
        return currentWorld;
    }

    public static void enter(BlockPos pos) {
        tracked = pos;
    }

    public static void enter(BlockEntityTickInvoker blockEntityTickInvoker) {
        tracked = blockEntityTickInvoker;
    }

    public static void enter(Entity entity) {
        tracked = entity;
    }

    public static void exit() {
        tracked = null;
    }

    public static boolean running() {
        return RUN_LOCK.isLocked() || STOP_LOCK.isLocked();
    }

    public static Object getCurrent() {
        Object o = tracked;
        return o instanceof BlockEntityTickInvoker invoker ? invoker.getPos() : o;
    }

    public static boolean tryStart(
        BlockBox box,
        long length,
        RedfileTimeUnit unit,
        RedfileInstance.DetailEnum detail,
        boolean doLoad,
        boolean doSprint,
        ServerCommandSource source
    ) {
        return INSTANCES
            .computeIfAbsent(source.getWorld().getRegistryKey(), x -> new ArrayList<>())
            .add(RedfileInstance.start(box, length, unit, detail, doLoad, doSprint, source));
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
